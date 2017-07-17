package dliehr.com.fahrtenbuch;

/**
 * Created by Dominik on 24.08.16.
 */
public class Errors {
    public static Error no_bluetooth_device_found = new Error(1, "no bluetooth device found");
    public static Error bluetooth_not_enabled = new Error(2, "bluetooth not enabled");
    public static Error no_gps_provider_enabled = new Error(3, "no gps provider enabled");
    public static Error gettings_gps_location_not_allowed = new Error(4, "gettings gps location not allowed");
    public static Error no_location_manager_existing = new Error(5, "no location manager existing");
    public static Error no_addresses_available = new Error(6, "no addresses available");
    public static Error thread_sleep_not_functional = new Error(7, "thread sleep not functional");
    public static Error location_is_null = new Error(8, "location is null");
    public static Error gps_not_enabled = new Error(9, "gps sensor not enabled");
    public static Error time_thread_date_format_error = new Error(10, "error in date formating in time thread");
    public static Error inserting_fahrtitem_not_possible = new Error(11, "error in inserting a new fahrtitem, not possible");
    public static Error updating_table_t_fahrt = new Error(12, "error in updating table t_fahrt");
    public static Error sending_email = new Error(13, "sending email error");
    public static Error warning_checking_on_start_for_existing_drive = new Error(14, "warning on checking for start for existing drive (no existing drive)");
    public static Error could_not_close_bluetooth_connection = new Error(15, "could not close bluetooth connection, because there is no active connection");
    public static Error could_not_load_last_drive = new Error(16, "could not load last drive, because there are no drives");
    public static Error could_not_delete_db_entries = new Error(17, "could not delete db entries");
}