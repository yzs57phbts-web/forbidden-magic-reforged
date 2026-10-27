package local.forbiddenmagic;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;

public final class CuratedAspects {
    private static final Logger LOGGER = LogUtils.getLogger();
    public record Rule(String mod, String family, boolean entity, List<ResourceLocation> targets, Map<Aspect, Integer> amounts) {
        public AspectList aspects() {
            AspectList out = new AspectList();
            amounts.forEach((aspect,amount)->{if(aspect!=Aspect.LUXURIA||!LegacyConfig.bool("noLust"))out.add(aspect,amount);});
            return out;
        }
    }
    private static final List<Rule> RULES = load();
    private CuratedAspects() {}

    public static List<Rule> rules() { return RULES; }

    private static List<Rule> load() {
        var stream = CuratedAspects.class.getResourceAsStream("/forbiddenmagic/aspects.json");
        if (stream == null) throw new IllegalStateException("Missing Forbidden Magic aspect rules");
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var data = JsonParser.parseReader(reader).getAsJsonObject();
            if (data.get("schema").getAsInt() != 1) throw new IllegalArgumentException("Unknown aspect schema");
            List<Rule> out = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            Set<String> supported = Set.of("botania", "bloodmagic", "witchery", "twilightforest");
            for (var value : data.getAsJsonArray("rules")) {
                var row = value.getAsJsonObject();
                String mod = row.get("mod").getAsString();
                if (!supported.contains(mod)) throw new IllegalArgumentException("Unsupported namespace " + mod);
                String kind = row.get("kind").getAsString();
                if (!kind.equals("item") && !kind.equals("entity")) throw new IllegalArgumentException("Unknown kind " + kind);
                Map<Aspect, Integer> amounts = new EnumMap<>(Aspect.class);
                row.getAsJsonObject("aspects").entrySet().forEach(entry -> {
                    Aspect aspect = Aspect.getAspect(entry.getKey());
                    int count = entry.getValue().getAsInt();
                    if (aspect == null || count < 1 || count > 16) throw new IllegalArgumentException("Invalid aspect " + entry);
                    amounts.put(aspect, count);
                });
                if (amounts.isEmpty() || amounts.size() > 5) throw new IllegalArgumentException("Unbounded aspect family");
                List<ResourceLocation> targets = new ArrayList<>();
                for (var target : row.getAsJsonArray("targets")) {
                    var id = ResourceLocation.parse(target.getAsString());
                    if (!id.getNamespace().equals(mod) || !seen.add(kind + ":" + id)) throw new IllegalArgumentException("Wrong/duplicate target " + id);
                    targets.add(id);
                }
                if (targets.isEmpty()) throw new IllegalArgumentException("Empty aspect family");
                out.add(new Rule(mod, row.get("family").getAsString(), kind.equals("entity"), List.copyOf(targets), Collections.unmodifiableMap(new EnumMap<>(amounts))));
            }
            return List.copyOf(out);
        } catch (Exception error) {
            throw new IllegalStateException("Cannot load curated aspect rules", error);
        }
    }

    /** Called inside Thaumcraft's rebuild, before recipe derivation and normal network sync. */
    public static void register(AspectRegistry registry) {
        int items = 0, entities = 0, missing = 0;
        for (Rule rule : RULES) {
            if (!LegacyConfig.integration(rule.mod())) continue;
            for (var id : rule.targets()) {
                if (rule.entity()) {
                    var type = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
                    if (type.isPresent()) { registry.registerEntity(type.get(), rule.aspects()); entities++; }
                    else { LOGGER.warn("Forbidden Magic: absent entity {} ({})", id, rule.family()); missing++; }
                } else {
                    var item = BuiltInRegistries.ITEM.getOptional(id);
                    if (item.isPresent()) { registry.register(item.get(), rule.aspects()); items++; }
                    else { LOGGER.warn("Forbidden Magic: absent item {} ({})", id, rule.family()); missing++; }
                }
            }
        }
        LOGGER.info("Forbidden Magic aspects: {} item/block targets, {} entities, {} missing targets", items, entities, missing);
    }
}
