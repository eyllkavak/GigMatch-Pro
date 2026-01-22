/**
 * A static utility class responsible for storing and providing fixed
 * configuration data related to the various service types offered by the platform.
 * It acts as a lookup table, linking service names to their corresponding
 * expected Skill Profiles.
 */
public class ServiceTypeManager {

     //Array containing the official, predefined names for all service categories.
    public static final String[] serviceNames = {
            "paint", "web_dev", "graphic_design", "data_entry", "tutoring",
            "cleaning", "writing", "photography", "plumbing", "electrical"
    };

     //Matrix defining the baseline skill requirements (profiles) for each service type.
    public static final int[][] skillProfiles = {
            {70, 60, 50, 85, 90},     // paint
            {95, 75, 85, 80, 90},     // web_dev
            {75, 85, 95, 70, 85},     // graphic_design
            {50, 50, 30, 95, 95},     // data_entry
            {80, 95, 70, 90, 75},     // tutoring
            {40, 60, 40, 90, 85},     // cleaning
            {70, 85, 90, 80, 95},     // writing
            {85, 80, 90, 75, 90},     // photography
            {85, 65, 60, 90, 85},     // plumbing
            {90, 65, 70, 95, 95}      // electrical
    };

    /**
     * Retrieves the zero-based index of a given service name within the static array.
     * Mechanism:Performs a linear search across the {@code serviceNames} array.
     * @param name The string name of the service (e.g., "web_dev").
     * @return The index of the service, or -1 if the name is not found.
     */
    public static int getServiceIndex(String name) {
        for (int i = 0; i < serviceNames.length; i++)
            if (serviceNames[i].equals(name))
                return i;
        return -1;
    }

    /**
     * Retrieves the predefined skill profile array for a specific service name.
     * Mechanism:First finds the index using {@code getServiceIndex}, then uses
     * that index to access the corresponding skill profile in {@code skillProfiles}.
     * @param name The string name of the service.
     * @return An array of integers representing the skill profile, or null if the service name is invalid.
     */
    public static int[] getSkillProfile(String name) {
        int idx = getServiceIndex(name);
        if (idx == -1) return null;
        return skillProfiles[idx];
    }
}