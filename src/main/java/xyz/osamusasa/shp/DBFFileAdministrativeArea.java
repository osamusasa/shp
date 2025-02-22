/**
 * Object of DBF file that contains administrative district data.
 */

package xyz.osamusasa.shp;

import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DBFFileAdministrativeArea {
    // 全データ
    Map<Integer, DbfRow> data;
    // 市町村名->List<Object ID>のMap
    Map<String, List<Integer>> cities;

    public DBFFileAdministrativeArea(Path p) {
        data = new HashMap<>();
        try {
            DBFReader reader = new DBFReader(Files.newInputStream(p), Charset.forName("SJIS"));
            DBFRow row;
            /*
              header
               [OBJECTID, N03_001, N03_002, N03_003, N03_004, N03_007, Shape_Leng, Shape_Area]
                 OBJECTID:   ID
                 N03_001:    県       都道府県名
                 N03_002:            支庁・振興局名
                 N03_003:    市       郡・政令都市名
                 N03_004:    市+区     市区町村名
                 N03_007:    標準地域コード
                 Shape_Leng: ?
                 Shape_Area: ?
             */
            while ((row = reader.nextRow()) != null) {
                DbfRow item = new DbfRow();
                item.objectId = row.getBigDecimal("OBJECTID").intValueExact();
                item.prefecture = row.getString("N03_001");
                item.promotionBureau = row.getString("N03_002");
                item.designatedCities = row.getString("N03_003");
                item.cities = row.getString("N03_004");
                item.code = row.getString("N03_007");
                item.shapeLen = row.getBigDecimal("Shape_Leng");
                item.shapeArea = row.getBigDecimal("Shape_Area");

                data.put(item.objectId, item);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        createNoDuplicateCites();
    }

    /**
     * 変数citesを初期化
     */
    private void createNoDuplicateCites() {
        cities = new HashMap<>();

        for (int i : data.keySet()) {
            String v = data.get(i).cities;
            if (cities.containsKey(v)) {
                cities.get(v).add(data.get(i).objectId);
            } else {
                List<Integer> l = new ArrayList<>();
                for (int ii : data.keySet()) {
                    if (data.get(ii).cities.equals(v)) {
                        l.add(data.get(ii).objectId);
                    }
                }
                cities.put(v, l);
            }
        }
    }

    /**
     * 重複なしの市区町村名を取得
     * @return 重複なしの市区町村名
     */
    public List<String> getCityName() {
        return cities.keySet()
                .stream()
                .sorted(Comparator.naturalOrder()).
                collect(Collectors.toList());
    }
}


class DbfRow{
    int objectId;
    String prefecture;
    String promotionBureau;
    String designatedCities;
    String cities;
    String code;
    BigDecimal shapeLen;
    BigDecimal shapeArea;
}
