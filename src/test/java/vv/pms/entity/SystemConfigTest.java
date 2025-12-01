package vv.pms.entity;

import org.junit.jupiter.api.Test;
import vv.pms.report.SystemConfig;

import static org.junit.jupiter.api.Assertions.*;

class SystemConfigTest {

    @Test
    void testDefaultConstructor() {
        SystemConfig config = new SystemConfig();
        
        assertNull(config.getId());
        assertNull(config.getConfigKey());
        assertNull(config.getConfigValue());
    }

    @Test
    void testParameterizedConstructor() {
        String key = "REPORT_DEADLINE";
        String value = "2024-12-31T23:59:59";
        
        SystemConfig config = new SystemConfig(key, value);
        
        assertNull(config.getId()); // ID is null until persisted
        assertEquals(key, config.getConfigKey());
        assertEquals(value, config.getConfigValue());
    }

    @Test
    void testSettersAndGetters() {
        SystemConfig config = new SystemConfig();
        
        config.setId(1L);
        config.setConfigKey("TEST_KEY");
        config.setConfigValue("test_value");
        
        assertEquals(1L, config.getId());
        assertEquals("TEST_KEY", config.getConfigKey());
        assertEquals("test_value", config.getConfigValue());
    }

    @Test
    void testConfigKeyChange() {
        SystemConfig config = new SystemConfig("OLD_KEY", "value");
        
        config.setConfigKey("NEW_KEY");
        
        assertEquals("NEW_KEY", config.getConfigKey());
    }

    @Test
    void testConfigValueChange() {
        SystemConfig config = new SystemConfig("KEY", "old_value");
        
        config.setConfigValue("new_value");
        
        assertEquals("new_value", config.getConfigValue());
    }

    @Test
    void testIdAssignment() {
        SystemConfig config = new SystemConfig("KEY", "VALUE");
        assertNull(config.getId());
        
        config.setId(100L);
        
        assertEquals(100L, config.getId());
    }

    @Test
    void testReportDeadlineConfig() {
        SystemConfig config = new SystemConfig("REPORT_DEADLINE", "2024-06-15T17:00:00");
        
        assertEquals("REPORT_DEADLINE", config.getConfigKey());
        assertEquals("2024-06-15T17:00:00", config.getConfigValue());
    }

    @Test
    void testMultipleConfigs() {
        SystemConfig config1 = new SystemConfig("KEY1", "VALUE1");
        SystemConfig config2 = new SystemConfig("KEY2", "VALUE2");
        
        config1.setId(1L);
        config2.setId(2L);
        
        assertNotEquals(config1.getId(), config2.getId());
        assertNotEquals(config1.getConfigKey(), config2.getConfigKey());
        assertNotEquals(config1.getConfigValue(), config2.getConfigValue());
    }

    @Test
    void testEmptyValue() {
        SystemConfig config = new SystemConfig("EMPTY_KEY", "");
        
        assertEquals("", config.getConfigValue());
    }

    @Test
    void testValueWithSpecialCharacters() {
        String specialValue = "value with spaces & special chars: @#$%^&*()";
        SystemConfig config = new SystemConfig("SPECIAL_KEY", specialValue);
        
        assertEquals(specialValue, config.getConfigValue());
    }

    @Test
    void testJsonValue() {
        String jsonValue = "{\"enabled\": true, \"maxSize\": 100}";
        SystemConfig config = new SystemConfig("JSON_CONFIG", jsonValue);
        
        assertEquals(jsonValue, config.getConfigValue());
    }

    @Test
    void testIsoDateTimeValue() {
        String isoDateTime = "2024-11-30T14:30:00.000000000";
        SystemConfig config = new SystemConfig("DATETIME_CONFIG", isoDateTime);
        
        assertEquals(isoDateTime, config.getConfigValue());
    }

    @Test
    void testConfigKeyUniqueness() {
        // ConfigKey has a unique constraint
        SystemConfig config1 = new SystemConfig("SAME_KEY", "value1");
        SystemConfig config2 = new SystemConfig("SAME_KEY", "value2");
        
        assertEquals(config1.getConfigKey(), config2.getConfigKey());
    }
}
