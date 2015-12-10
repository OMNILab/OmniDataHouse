package cn.edu.sjtu.omnilab.odh.rawfilter;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	private Map<String, String> APBN_RealBN_Cache = new HashMap<String, String>();

	public APToBuilding(){
		this(APToBuilding.class.getResourceAsStream(AP_BUILDING_DATABASE), true);
	}

	public APToBuilding(boolean full_apname){
		this(APToBuilding.class.getResourceAsStream(AP_BUILDING_DATABASE), full_apname);
	}

	@SuppressWarnings("unchecked")
	public APToBuilding(InputStream APDBYAML, boolean full_apname) {
		this.full_apname = full_apname;

		// Load yaml database
		Yaml yaml = new Yaml(new SafeConstructor());
		Map<String,Object> regexConfig = (Map<String,Object>) yaml.load(APDBYAML);
	    APNameDB = (Map<String, Map<String, String>>) regexConfig.get("apprefix_sjtu");
	}

	public List<String> parse(String APName){
		List<String> result = null;

		if ( APName == null )
			return result;
		
		if ( full_apname )  { // Given full AP name string
			String[] parts = APName.split("-\\d+F-", 2);
			String buildName = parts[0];

			// Remove MH- prefix
			if (buildName.startsWith("MH-"))
				buildName = buildName.substring(3, buildName.length());

			// Check cache first
			if ( APBN_RealBN_Cache.containsKey(buildName) ) { // Cache hit
				String cacheRealBN = APBN_RealBN_Cache.get(buildName);
				result = getBuildInfo(cacheRealBN);
			} else {

			    // Cache miss
				if ( APNameDB.containsKey(buildName)) {
					result = getBuildInfo(buildName);
					APBN_RealBN_Cache.put(buildName, buildName);
				} else {
					// Worst case; try to find its longest matched building name
					String realBuildName = null;

					for ( String BN : APNameDB.keySet())
						if ( buildName.contains(BN) )
							if ( realBuildName == null )
								realBuildName = BN;
							else if ( BN.length() > realBuildName.length() )
								realBuildName = BN; // Get the longest match

					if ( realBuildName != null ){
						result = getBuildInfo(realBuildName);
						// Cache the real building name
						APBN_RealBN_Cache.put(buildName, realBuildName);
					}
				}
			}
		} else { // Given build name, skip cache actions

			if ( APNameDB.containsKey(APName) ) // Have item
				result = getBuildInfo(APName);

		}

		return result;
	}
	
	private List<String> getBuildInfo(String realBuildName){

		List info = new LinkedList<String>();
		Map<String, String> buildInfo = APNameDB.get(realBuildName);

		info.add(buildInfo.get("name"));
		info.add(buildInfo.get("type"));
		info.add(buildInfo.get("user"));
        info.add(buildInfo.get("lat"));
        info.add(buildInfo.get("lon"));

		return info;
	}
}
