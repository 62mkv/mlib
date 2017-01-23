package com.sam.deal.api.poldat;

import java.util.Map;
import java.util.HashMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.sam.moca.EditableResults;
import com.sam.moca.MocaArgumentException;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.SimpleResults;
import com.sam.moca.util.MocaUtils;

class Poldat {
    public String polcod;
    public String polvar;
    public String polval;
    public String grp_id;
    public int srtseq;
    public String rtstr1;
    public String rtstr2;
    public int rtnum1;
    public int rtnum2;
    public double rtflt1;
    public double rtflt2;
    public String moddte;
    public String mod_usr_id;
}

public class PolicyManager {
    static Logger log = LogManager.getLogger(PolicyManager.class);
    static private Map<String, Poldat> poldat_cache = new HashMap<String, Poldat>();
    static private MocaContext _moca = null;

    static String buildPoldatKey(String grp_id, String polcod, String polvar,
            String polval, Integer srtseq) {
        String polKey = "";
        if (srtseq == null) {
            polKey = grp_id + '|' + polcod + '|' + polvar + '|' + polval;
        } else {
            polKey = grp_id + '|' + polcod + '|' + polvar + '|' + polval + '|'
                    + srtseq;
        }
        return polKey;
    }

    static public MocaResults createPolicy(String grp_id, String polcod,
            String polvar, String polval, Integer srtseq, String rtstr1,
            String rtstr2, Integer rtnum1, Integer rtnum2, Double rtflt1,
            Double rtflt2)
            throws MocaArgumentException {

        if (grp_id == null || grp_id.isEmpty()) {
            throw new MocaArgumentException("grp_id is required!");
        }
        if (polcod == null || polcod.isEmpty()) {
            throw new MocaArgumentException("polcod is required!");
        }
        if (polvar == null || polvar.isEmpty()) {
            throw new MocaArgumentException("polvar is required!");
        }
        if (polval == null || polval.isEmpty()) {
            throw new MocaArgumentException("polval is required!");
        }

        if (srtseq == null) {
            srtseq = 0;
        }
        
        String cls =  " where grp_id = '" + grp_id +
                "' and polcod = '" + polcod  +
                "' and polvar = '" + polvar +
                "' and polval = '" + polval +
                "' and srtseq = " + (srtseq == null ? 0 : srtseq);
  
        if (rtstr1 != null && !rtstr1.isEmpty()) {
            cls += " and rtstr1 ='" + rtstr1 + "'";
        }
        
        if (rtstr2 != null && !rtstr2.isEmpty()) {
            cls += " and rtstr2 ='" + rtstr2 + "'";
        }
        
        if (rtnum1 != null) {
            cls += " and rtnum1 =" + rtnum1;
        }
        
        if (rtnum2 != null) {
            cls += " and rtnum2 =" + rtnum2;
        }
        
        if (rtflt1 != null) {
            cls += " and rtflt1 =" + rtflt1;
        }
        
        if (rtflt2 != null) {
            cls += " and rtflt2 =" + rtflt2;
        }

        EditableResults res = new SimpleResults();
        res.addColumn("grp_id", MocaType.STRING);
        res.addColumn("polcod", MocaType.STRING);
        res.addColumn("polvar", MocaType.STRING);
        res.addColumn("polval", MocaType.STRING);
        res.addColumn("srtseq", MocaType.INTEGER);
        res.addColumn("rtstr1", MocaType.STRING);
        res.addColumn("rtstr2", MocaType.STRING);
        res.addColumn("rtnum1", MocaType.INTEGER);
        res.addColumn("rtnum2", MocaType.INTEGER);
        res.addColumn("rtflt1", MocaType.DOUBLE);
        res.addColumn("rtflt2", MocaType.DOUBLE);

        try {
            _moca = MocaUtils.currentContext();

            log.info("now creating poldat record.");
            _moca.executeCommand(" publish data " + cls + "|"
                    + " create record where table_name = 'poldat' "
                    + "    and @* ");

            Poldat pd = loadPolicy(grp_id, polcod, polvar, polval, srtseq, true);

            String polKey = buildPoldatKey(grp_id, polcod, polvar, polval,
                    srtseq);

            log.info("now caching poldat.");
            poldat_cache.put(polKey, pd);

            res.addRow();
            res.setStringValue("grp_id", pd.grp_id);
            res.setStringValue("polcod", pd.polcod);
            res.setStringValue("polvar", pd.polvar);
            res.setStringValue("polval", pd.polval);
            res.setIntValue("srtseq", pd.srtseq);
            res.setStringValue("rtstr1", pd.rtstr1);
            res.setStringValue("rtstr2", pd.rtstr2);
            res.setIntValue("rtnum1", pd.rtnum1);
            res.setIntValue("rtnum2", pd.rtnum2);
            res.setDoubleValue("rtflt1", pd.rtflt1);
            res.setDoubleValue("rtflt2", pd.rtflt2);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return res;
    }

    static public MocaResults changePolicy(String polcod,
            String polvar, String polval, Integer srtseq, String rtstr1,
            String rtstr2, Integer rtnum1, Integer rtnum2, Double rtflt1,
            Double rtflt2, String grp_id)
            throws MocaArgumentException {

        if (grp_id == null || grp_id.isEmpty()) {
            throw new MocaArgumentException("grp_id is required!");
        }
        if (polcod == null || polcod.isEmpty()) {
            throw new MocaArgumentException("polcod is required!");
        }
        if (polvar == null || polvar.isEmpty()) {
            throw new MocaArgumentException("polvar is required!");
        }
        if (polval == null || polval.isEmpty()) {
            throw new MocaArgumentException("polval is required!");
        }

        String cls =  " where grp_id = '" + grp_id +
                      "' and polcod = '" + polcod  +
                      "' and polvar = '" + polvar +
                      "' and polval = '" + polval +
                      "' and srtseq = " + (srtseq == null ? 0 : srtseq);
        
        if (rtstr1 != null && !rtstr1.isEmpty()) {
            cls += " and rtstr1 ='" + rtstr1 + "'";
        }
        
        if (rtstr2 != null && !rtstr2.isEmpty()) {
            cls += " and rtstr2 ='" + rtstr2 + "'";
        }
        
        if (rtnum1 != null) {
            cls += " and rtnum1 =" + rtnum1;
        }
        
        if (rtnum2 != null) {
            cls += " and rtnum2 =" + rtnum2;
        }
        
        if (rtflt1 != null) {
            cls += " and rtflt1 =" + rtflt1;
        }
        
        if (rtflt2 != null) {
            cls += " and rtflt2 =" + rtflt2;
        }
        
        EditableResults res = new SimpleResults();
        res.addColumn("grp_id", MocaType.STRING);
        res.addColumn("polcod", MocaType.STRING);
        res.addColumn("polvar", MocaType.STRING);
        res.addColumn("polval", MocaType.STRING);
        res.addColumn("srtseq", MocaType.INTEGER);
        res.addColumn("rtstr1", MocaType.STRING);
        res.addColumn("rtstr2", MocaType.STRING);
        res.addColumn("rtnum1", MocaType.INTEGER);
        res.addColumn("rtnum2", MocaType.INTEGER);
        res.addColumn("rtflt1", MocaType.DOUBLE);
        res.addColumn("rtflt2", MocaType.DOUBLE);

        try {
            _moca = MocaUtils.currentContext();

            log.info("now changing poldat record.");
            _moca.executeCommand(" publish data " + cls
                    + "|"
                    + " change record where table_name = 'poldat' "
                    + "    and @* ");

            Poldat pd = loadPolicy(grp_id, polcod, polvar, polval, srtseq, true);

            String polKey = buildPoldatKey(grp_id, polcod, polvar, polval,
                    srtseq);

            log.info("now caching poldat.");
            poldat_cache.put(polKey, pd);

            res.addRow();
            res.setStringValue("grp_id", pd.grp_id);
            res.setStringValue("polcod", pd.polcod);
            res.setStringValue("polvar", pd.polvar);
            res.setStringValue("polval", pd.polval);
            res.setIntValue("srtseq", pd.srtseq);
            res.setStringValue("rtstr1", pd.rtstr1);
            res.setStringValue("rtstr2", pd.rtstr2);
            res.setIntValue("rtnum1", pd.rtnum1);
            res.setIntValue("rtnum2", pd.rtnum2);
            res.setDoubleValue("rtflt1", pd.rtflt1);
            res.setDoubleValue("rtflt2", pd.rtflt2);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return res;
    }

    static public MocaResults removePolicy(String grp_id, String polcod,
            String polvar, String polval, Integer srtseq)
            throws MocaArgumentException {

        if (grp_id == null || grp_id.isEmpty()) {
            throw new MocaArgumentException("grp_id is required!");
        }
        if (polcod == null || polcod.isEmpty()) {
            throw new MocaArgumentException("polcod is required!");
        }
        if (polvar == null || polvar.isEmpty()) {
            throw new MocaArgumentException("polvar is required!");
        }
        if (polval == null || polval.isEmpty()) {
            throw new MocaArgumentException("polval is required!");
        }

        if (srtseq == null) {
            srtseq = 0;
        }

        EditableResults res = new SimpleResults();
        res.addColumn("grp_id", MocaType.STRING);
        res.addColumn("polcod", MocaType.STRING);
        res.addColumn("polvar", MocaType.STRING);
        res.addColumn("polval", MocaType.STRING);
        res.addColumn("srtseq", MocaType.INTEGER);
        res.addColumn("rtstr1", MocaType.STRING);
        res.addColumn("rtstr2", MocaType.STRING);
        res.addColumn("rtnum1", MocaType.INTEGER);
        res.addColumn("rtnum2", MocaType.INTEGER);
        res.addColumn("rtflt1", MocaType.DOUBLE);
        res.addColumn("rtflt2", MocaType.DOUBLE);
        res.addColumn("moddte", MocaType.STRING);
        res.addColumn("mod_usr_id", MocaType.STRING);

        try {
            _moca = MocaUtils.currentContext();

            Poldat pd = loadPolicy(grp_id, polcod, polvar, polval, srtseq, true);

            log.info("now removing poldat record.");
            _moca.executeCommand(" publish data " + "    where grp_id = '"
                    + grp_id + "'     and polcod = '" + polcod
                    + "'     and polvar = '" + polvar + "'     and polval = '"
                    + polval + "'     and srtseq = " + srtseq + "|"
                    + " remove record where table_name = 'poldat' "
                    + "    and @* ");

            String polKey = buildPoldatKey(grp_id, polcod, polvar, polval,
                    srtseq);

            log.info("now clear cache for poldat, key:" + polKey);
            poldat_cache.remove(polKey, pd);

            res.addRow();
            res.setStringValue("grp_id", pd.grp_id);
            res.setStringValue("polcod", pd.polcod);
            res.setStringValue("polvar", pd.polvar);
            res.setStringValue("polval", pd.polval);
            res.setIntValue("srtseq", pd.srtseq);
            res.setStringValue("rtstr1", pd.rtstr1);
            res.setStringValue("rtstr2", pd.rtstr2);
            res.setIntValue("rtnum1", pd.rtnum1);
            res.setIntValue("rtnum2", pd.rtnum2);
            res.setDoubleValue("rtflt1", pd.rtflt1);
            res.setDoubleValue("rtflt2", pd.rtflt2);
            res.setStringValue("moddte", pd.moddte);
            res.setStringValue("mod_usr_id", pd.mod_usr_id);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return res;
    }

    static public Poldat loadPolicy(String grp_id, String polcod, String polvar,
            String polval, Integer srtseq, boolean skipCache) {
        String polKey = buildPoldatKey(grp_id, polcod, polvar, polval, srtseq);
        Poldat pd = null;
        if (!skipCache) {
            poldat_cache.get(polKey);
        }
        if (pd == null) {
            try {
                _moca = MocaUtils.currentContext();
                MocaResults rs = _moca.executeCommand(
                        "[select * from poldat " + "  where grp_id = '" + grp_id
                                + "'" + "    and polcod = '" + polcod + "'"
                                + "    and polvar = '" + polvar + "'"
                                + "    and polval = '" + polval + "'"
                                + "    and srtseq = "
                                + (srtseq == null ? "srtseq" : srtseq) + "]");
                rs.next();
                pd = new Poldat();
                pd.grp_id = rs.getString("grp_id");
                pd.polcod = rs.getString("polcod");
                pd.polvar = rs.getString("polvar");
                pd.polval = rs.getString("polval");
                pd.srtseq = rs.getInt("srtseq");
                pd.rtstr1 = rs.getString("rtstr1");
                pd.rtstr2 = rs.getString("rtstr2");
                pd.rtnum1 = rs.getInt("rtnum1");
                pd.rtnum2 = rs.getInt("rtnum2");
                pd.rtflt1 = rs.getDouble("rtflt1");
                pd.rtflt2 = rs.getDouble("rtflt2");
                pd.moddte = rs.getString("moddte");
                pd.mod_usr_id = rs.getString("mod_usr_id");

                poldat_cache.put(polKey, pd);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
                return null;
            }
        }
        return pd;
    }

    static public MocaResults listPolicies(String grp_id, String polcod, String polvar,
            String polval, Integer srtseq) {
        
        String cls = " 1= 1";
        
        if (grp_id != null && !grp_id.isEmpty()) {
            cls += " and grp_id ='" + grp_id + "'";
        }
        
        if (polcod != null && !polcod.isEmpty()) {
            cls += " and polcod ='" + polcod + "'";
        }
        
        if (polvar != null && !polvar.isEmpty()) {
            cls += " and polvar ='" + polvar + "'";
        }
        
        if (polval != null && !polval.isEmpty()) {
            cls += " and polval ='" + polval + "'";
        }
        
        if (srtseq != null) {
            cls += " and srtseq = " + srtseq;
        }
        MocaResults res = null;
        try {
            _moca = MocaUtils.currentContext();
            res = _moca.executeCommand(
                    "[select * from poldat where " + cls + "]");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return null;
        }
        return res;
    }
}