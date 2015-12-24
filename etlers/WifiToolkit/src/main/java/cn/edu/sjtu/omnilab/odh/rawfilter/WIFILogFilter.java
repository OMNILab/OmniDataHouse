package cn.edu.sjtu.omnilab.odh.rawfilter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Cleanse and format Aruba Wifi logs by filtering out unwanted log numbers.
 *
 * Input:
 * Raw or original Aruba syslog files.
 *
 * Output:
 * 6c71d96d8c4d,2013-10-11 23:50:53,0,XXY-3F-09
 * 6c71d96d8c4d,2013-10-11 23:50:53,2,XXY-3F-09
 *
 * Message encoding: see WifiCode.java
 *
 * BN:
 * Before Oct. 14, 2013, WiFi networks in SJTU deploy the IP address of 111.186.0.0/18.
 * But after that (or confirmedly Oct. 17, 2013), local addresses are utlized to save the IP
 * resources. New IP addresses deployed in WiFi networks are:
 * Local1: 1001 111.186.16.1/20, New: 10.185.0.0/16
 * Local2: 1001 111.186.33.1/21, New: 10.186.0.0/16
 * Local3: 1001 111.186.40.1/21, New: 10.188.0.0/16
 * Local4: 1001 111.186.48.1/20, New: 10.187.0.0/16
 * Local5: 1001 111.186.0.1/20, New: 10.184.0.0/16
 *
 * We also lost year in time for most months of 2013.
 * This requires fix in the pattern matching.
 *
 *
 * @Author chenxm, gwj
 */
public class WIFILogFilter {

    /**
     * Filter the raw wifilogs with regex utilities.
     *
     * @param rawLogEntry
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static String filterData(String rawLogEntry) throws IOException {

        // Message codes selected for mobility
        final int[] CODE_AUTHREQ = {501091, 501092, 501109};
        final int[] CODE_AUTHRES = {501093, 501094, 501110};
        final int[] CODE_DEAUTH = {501105, 501080, 501098, 501099, 501106, 501107, 501108, 501111}; // from and to
        final int[] CODE_ASSOCREQ = {501095, 501096, 501097};
        final int[] CODE_ASSOCRES = {501100, 501101, 501112};
        final int[] CODE_DISASSOCFROM = {501102, 501104, 501113};
        final int[] CODE_USERAUTH = {522008, 522042, 522038}; // Successful and failed
        final int[] CODE_USRSTATUS = {522005, 522006, 522026}; // User Entry added, deleted, and user miss
        final int[] CODE_USERROAM = {500010};
        final int[] CODE_NEW_DEV = {522035};

        // Regex for timestamp in e.g. "Dec 14 15:45:05 2015"
        final String regTime = "(\\w+\\s+\\d+\\s+(?:\\d{1,2}:){2}\\d{1,2}(?:\\s+\\d{4})?)";

        // Regex for user MAC address like f4:29:81:e3:7c:1f
        final String regUserMac = "((?:[0-9a-f]{2}:){5}[0-9a-f]{2})";

        // Regex for IP addresses like 10.188.19.45
        final String regIPAddr = "((?:\\d{1,3}\\.){3}\\d{1,3})";

        // Regex for IP addresses with specific range as stated in class doc.
        final String regIPRange = "((?:111\\.\\d+|10\\.18[4-8])(?:\\.\\d+){2})";

        // Regex for AP name, e.g. "CL-A-4F-04"
        final String regApName = "([\\w-]+)";

        // Regex for AP info such as "10.192.32.69-00:24:6c:59:b6:33-MH-JZG-10#-OUT"
        final String regApInfo = "((?:\\d{1,3}\\.){3}\\d{1,3})-((?:[0-9a-f]{2}:){5}[0-9a-f]{2})-([\\w-]+)";

        // Regex for roaming info such as "SJTU-Web/d8:c7:c8:28:ff:b9/a"
        final String regRoamInfo = "([\\w-]+)/((?:[0-9a-f]{2}:){5}[0-9a-f]{2})/(\\w+)";

        // time: group(1), usename: group(2), apip: group(3), apmac: group(4), apname: group(5)
        final Pattern REG_AUTHREQ = Pattern.compile(String.format("%s(?:.*)Auth\\s+request:\\s+%s:?\\s+(?:.*)AP\\s+%s",
                regTime, regUserMac, regApInfo), Pattern.CASE_INSENSITIVE);

        // time: group(1), usename: group(2), apip: group(3), apmac: group(4), apname: group(5)
        final Pattern REG_AUTHRES = Pattern.compile(String.format("%s(?:.*)Auth\\s+(success|failure):\\s+%s:?\\s+AP\\s+%s",
                regTime, regUserMac, regApInfo), Pattern.CASE_INSENSITIVE);

        // time: group(1), usename: group(2), apip: group(3), apmac: group(4), apname: group(5)
        final Pattern REG_DEAUTH = Pattern.compile(String.format("%s(?:.*)Deauth(?:.*):\\s+%s:?\\s+(?:.*)AP\\s+%s",
                regTime, regUserMac, regApInfo), Pattern.CASE_INSENSITIVE);

        // time: group(1), usename: group(2), apip: group(3), apmac: group(4), apname: group(5)
        final Pattern REG_ASSOCREQ = Pattern.compile(String.format("%s(?:.*)Assoc(?:.*):\\s+%s(?:.*):?\\s+(?:.*)AP\\s+%s",
                regTime, regUserMac, regApInfo), Pattern.CASE_INSENSITIVE);

        // time: group(1), usename: group(2), apip: group(3), apmac: group(4), apname: group(5)
        final Pattern REG_DISASSOCFROM = Pattern.compile(String.format("%s(?:.*)Disassoc(?:.*):\\s+%s:?\\s+AP\\s+%s",
                regTime, regUserMac, regApInfo), Pattern.CASE_INSENSITIVE);

        // time: group(1), usename: group(2), usermac: group(3), userip: group(4), apname: group(5)
        final Pattern REG_USERAUTH = Pattern.compile(String.format("%s(?:.*)\\s+username=([^\\s]+)\\s+MAC=%s\\s+IP=%s(?:.+)(?:AP=([^\\s]+))?",
                regTime, regUserMac, regIPAddr), Pattern.CASE_INSENSITIVE);

        // time: group(1), usermac: group(2), userip: group(3)
        final Pattern REG_USRSTATUS = Pattern.compile(String.format("%s(?:.*)MAC=%s\\s+IP=%s",
                regTime, regUserMac, regIPRange), Pattern.CASE_INSENSITIVE);

        // time: group(1), usermac: group(2), userip: group(3), apname: group(4), essid: group(5), bssid: group(6), phy: group(7)
        final Pattern REG_USERROAM = Pattern.compile(String.format("%s(?:.*)Station\\s+%s,\\s+(?:%s)?:\\s+(?:.*)\\s+AP\\s+%s,\\s+%s",
                regTime, regUserMac, regIPAddr, regApName, regRoamInfo));

        // time: group(1), usermac: group(2), bssid: group(3), essid: group(4), apname: group(5)
        final Pattern REG_NEW_DEV = Pattern.compile(String.format("%s(?:.*)MAC=%s Station UP: BSSID=%s ESSID=%s (?:.*)AP-name=%s",
                regTime, regUserMac, regUserMac, regApName, regApName));


        String cleanLog = null;
        String[] chops = new String[0];
        try {
            chops = rawLogEntry.split("<", 3);
        } catch (Exception e) {
            // invalid syslog that is incomplete.
            return cleanLog;
        }

        if (chops.length < 3 || chops[2].length() == 0 || chops[2].charAt(0) != '5') {
            // invalid syslog that does not convey user's mobility info.
            return cleanLog;
        }

        int messageCode = Integer.valueOf(chops[2].split(">", 2)[0]);

        if (hasCodes(messageCode, CODE_AUTHREQ)) { // Auth request
            Matcher matcher = REG_AUTHREQ.matcher(rawLogEntry);
            if (matcher.find()) {
                String time = formattrans(matcher.group(1));
                String usermac = matcher.group(2).replaceAll(":", "");
                String apname = matcher.group(5);
                cleanLog = String.format("%s,%s,%s,%s", usermac, time, WIFICode.AuthRequest, apname);
            }
        } else if (hasCodes(messageCode, CODE_DEAUTH)) { // Deauth from and to
            Matcher matcher = REG_DEAUTH.matcher(rawLogEntry);
            if (matcher.find()) {
                String time = formattrans(matcher.group(1));
                String usermac = matcher.group(2).replaceAll(":", "");
                String apname = matcher.group(5);
                cleanLog = String.format("%s,%s,%s,%s", usermac, time, WIFICode.Deauth, apname);
            }
        } else if (hasCodes(messageCode, CODE_ASSOCREQ)) { // Association request
            Matcher matcher = REG_ASSOCREQ.matcher(rawLogEntry);
            if (matcher.find()) {
                String time = formattrans(matcher.group(1));
                String usermac = matcher.group(2).replaceAll(":", "");
                String apname = matcher.group(5);
                cleanLog = String.format("%s,%s,%s,%s", usermac, time, WIFICode.AssocRequest, apname);
            }
        } else if (hasCodes(messageCode, CODE_DISASSOCFROM)) { // Disassociation
            Matcher matcher = REG_DISASSOCFROM.matcher(rawLogEntry);
            if (matcher.find()) {
                String time = formattrans(matcher.group(1));
                String usermac = matcher.group(2).replaceAll(":", "");
                String apname = matcher.group(5);
                cleanLog = String.format("%s,%s,%s,%s", usermac, time, WIFICode.Disassoc, apname);
            }
        } else if (hasCodes(messageCode, CODE_USERAUTH)) {  //username information, User authentication
            Matcher matcher = REG_USERAUTH.matcher(rawLogEntry);
            if (matcher.find()) {
                String time = formattrans(matcher.group(1));
                String username = matcher.group(2);
                String usermac = matcher.group(3).replaceAll(":", "");
                String userip = matcher.group(4);
                String apname = null;
                try {
                    apname = matcher.group(5);
                } catch (Exception e) {
                    //apname is null if it is not there
                }
                cleanLog = String.format("%s,%s,%s,%s,%s,%s", usermac, time, WIFICode.UserAuth, username, userip, apname);
            }
        } else if (hasCodes(messageCode, CODE_USRSTATUS)) { // User entry update status
            Matcher matcher = REG_USRSTATUS.matcher(rawLogEntry);
            if (matcher.find()) {
                String time = formattrans(matcher.group(1));
                String usermac = matcher.group(2).replaceAll(":", "");
                String userip = matcher.group(3);
                int action = WIFICode.IPAllocation;    // IP bond
                if (messageCode == 522005) {
                    action = WIFICode.IPRecycle;
                }
                /* From the output, we see multiple IPAllocation message between
                 * the first allocation of specific IP and its recycling action.
                 */
                cleanLog = String.format("%s,%s,%s,%s", usermac, time, action, userip);
            }
        } else if (hasCodes(messageCode, CODE_USERROAM)) {
            Matcher matcher = REG_USERROAM.matcher(rawLogEntry);
            if (matcher.find()) {
                String time = formattrans(matcher.group(1));
                String usermac = matcher.group(2).replaceAll(":", "");
                String userip = matcher.group(3);
                String apname = matcher.group(4);
                cleanLog = String.format("%s,%s,%s,%s,%s", usermac, time, WIFICode.UserRoam, apname, userip);
            }
        } else if (hasCodes(messageCode, CODE_NEW_DEV)) {
            Matcher matcher = REG_NEW_DEV.matcher(rawLogEntry);
            if (matcher.find()) {
                String time = formattrans(matcher.group(1));
                String usermac = matcher.group(2).replaceAll(":", "");
                String apname = matcher.group(5);
                cleanLog = String.format("%s,%s,%s,%s", usermac, time, WIFICode.NewDev, apname);
            }
        }

        return cleanLog;
    }

    /**
     * Helper to check if specific message code is contained.
     *
     * @param messageCode
     * @param codes
     * @return
     */
    private static boolean hasCodes(int messageCode, int[] codes) {
        boolean flag = false;
        for (int i : codes) {
            if (messageCode == i) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * Put a user record into a map data structure.
     *
     * @param userMac
     * @param record
     * @param map
     * @return
     */
    private static int putRecordMap(String userMac, String record, Map<String, List<String>> map) {
        if (!map.containsKey(userMac)) {
            map.put(userMac, new LinkedList<String>());
        }
        map.get(userMac).add(record);
        return 0;
    }

    //This function is used to change the date format from "May 4" to "2013-05-04"
    private static String formattrans(String date_string){

        //Prepare for the month name for date changing
        TreeMap<String, String> month_tmap = new TreeMap<String, String>();
        month_tmap.put("Jan", "01");
        month_tmap.put("Feb", "02");
        month_tmap.put("Mar", "03");
        month_tmap.put("Apr", "04");
        month_tmap.put("May", "05");
        month_tmap.put("Jun", "06");
        month_tmap.put("Jul", "07");
        month_tmap.put("Aug", "08");
        month_tmap.put("Sep", "09");
        month_tmap.put("Oct", "10");
        month_tmap.put("Nov", "11");
        month_tmap.put("Dec", "12");

        //change the date from "May 4" to "2013-05-04"
        // month: group(1), day: group(2), time: group(3), year: group(4)
        String date_reg = "(\\w+)\\s+(\\d+)\\s+((?:\\d{1,2}:){2}\\d{1,2})(?:\\s+(\\d{4}))?";
        Pattern date_pattern = Pattern.compile(date_reg);
        Matcher date_matcher = date_pattern.matcher(date_string);
        if(! date_matcher.find())
            return null;

        String year_string = date_matcher.group(4);
        if ( year_string == null ) {
            // We may lose year before 2013
            year_string = "2013";
        }

        //change the month format
        String month_string = date_matcher.group(1);
        if(month_tmap.containsKey(month_string)){
            month_string = month_tmap.get(month_string);
        }else{
            System.out.println("Can not find the month!!!");
        }
        //change the day format
        String day_string = date_matcher.group(2);
        int day_int = Integer.parseInt(day_string);
        if(day_int < 10){
            day_string = "0" + Integer.toString(day_int);
        }else{
            day_string = Integer.toString(day_int);
        }
        String time_string = date_matcher.group(3);

        return String.format("%s-%s-%s %s", year_string, month_string, day_string, time_string);
    }
}
