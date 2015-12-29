package cn.edu.sjtu.omnilab.odh.rawfilter;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given a AP name string, this function return the description of the building.
 * Specifically for AP name, both full name string (the default mode, e.g.
 * BYGTSG-4F-01) and building name (e.g. BYGTSG) can be used.
 *
 * If only building name is given, you can save processing time to declare this
 * method with @full_apname param. But for accuracy, the full AP name is preferred.
 *
 * To update the database, visit
 *  https://github.com/caesar0301/omnilab-misc/tree/master/DBs/apmap
 *
 * @author chenxm
 */
public class APToBuilding {

	private static final String AP_BUILDING_DATABASE = "/apnames-utf8.yaml";
	private Map<String, Map<String, String>> APNameDB;
	private boolean full_apname = true;
	private Map<String, String> ApbnCache = new HashMap<String, String>();
    private final Pattern studorm = Pattern.compile("([DXEWSN]\\d+)-student|student-?([DXEWSN]\\d+)", Pattern.CASE_INSENSITIVE);


	public APToBuilding(){
		this(APToBuilding.class.getResourceAsStream(AP_BUILDING_DATABASE), true);
	}


	public APToBuilding(boolean full_apname){
		this(APToBuilding.class.getResourceAsStream(AP_BUILDING_DATABASE), full_apname);
	}


	@SuppressWarnings("unchecked")
	public APToBuilding(InputStream APDBYAML, boolean full_apname) {
		this.full_apname = full_apname;
		Yaml yaml = new Yaml(new SafeConstructor());
		Map<String,Object> regexConfig = (Map<String,Object>) yaml.load(APDBYAML);
	    APNameDB = (Map<String, Map<String, String>>) regexConfig.get("apprefix_sjtu");
	}


    /**
     * Convert a full AP name into bulding info.
     * @param APName The full AP name from WiFi syslog.
     */
	public List<String> parse(String APName){
		List<String> result = null;

		if ( APName == null )
			return result;
		
		if ( full_apname )  { // Given full AP name string
			String[] parts = APName.split("-\\d+F-", 2);
			String namestr = parts[0];

			// Remove MH- prefix
			if (namestr.startsWith("MH-"))
				namestr = namestr.substring(3, namestr.length());

			// Caching the mapping between extracted string and real building name
			if ( ApbnCache.containsKey(namestr) ) {

			    // Cache hit
				String cachehit = ApbnCache.get(namestr);
				result = getBuildInfo(cachehit);

			} else {

                // Handle student dorms
                Matcher matcher = studorm.matcher(namestr);
                if (matcher.find()) {
                    namestr = String.format("student-%s",
                            matcher.group(1)==null ? matcher.group(2) : matcher.group(1));
                }

			    // Cache miss
				if ( APNameDB.containsKey(namestr)) {
					result = getBuildInfo(namestr);
					ApbnCache.put(namestr, namestr);
				} else {
					// Worst case; try to find its longest matched building name
                    String rname = longestMatch(namestr);
                    if ( rname != null ){
                        result = getBuildInfo(rname);
                        ApbnCache.put(namestr, rname);
                    }
				}
			}
		} else {
			if ( APNameDB.containsKey(APName) ) // Have item
				result = getBuildInfo(APName);
		}

		return result;
	}


    /**
     * Find the name string in database via longest substring matching.
     * @param namestr The building name extracted from AP string.
     */
    private String longestMatch(String namestr) {
        String rname = null;

        for ( String bname : APNameDB.keySet())
            if ( namestr.contains(bname) )
                if ( rname == null )
                    rname = bname;
                else if ( bname.length() > rname.length() )
                    rname = bname; // Get the longest match

        return rname;
    }


    /**
     * Format building info. into string list.
     * @param rname The real building name in database;
     */
	private List<String> getBuildInfo(String rname){

		List info = new LinkedList<String>();
		Map<String, String> buildInfo = APNameDB.get(rname);

		info.add(buildInfo.get("name"));
		info.add(buildInfo.get("type"));
		info.add(buildInfo.get("user"));
        info.add(buildInfo.get("lat"));
        info.add(buildInfo.get("lon"));

		return info;
	}
}
