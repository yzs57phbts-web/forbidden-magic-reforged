package local.forbiddenmagic.tests;
import java.util.*;
import local.forbiddenmagic.LegacyConfig;
final class ConfigScope130 implements AutoCloseable {
    private final java.lang.reflect.Field field;private final Object before;
    ConfigScope130(Map<String,Object> overrides)throws Exception{
        field=LegacyConfig.class.getDeclaredField("values");field.setAccessible(true);before=field.get(null);
        @SuppressWarnings("unchecked") var copy=new LinkedHashMap<String,Object>((Map<String,Object>)before);copy.putAll(overrides);field.set(null,Collections.unmodifiableMap(copy));
    }
    @Override public void close()throws Exception{field.set(null,before);}
}
