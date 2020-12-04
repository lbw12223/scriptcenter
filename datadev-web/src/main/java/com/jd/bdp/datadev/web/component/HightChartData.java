package com.jd.bdp.datadev.web.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangrui25 on 2018/8/9.
 */
public class HightChartData {

    static Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");


    private static double getSum(List<JSONObject> sumDatas, String columnIndex) {
        double total = 0.0d;
        for (JSONObject data : sumDatas) {
            double tempValue = data.getDouble(columnIndex);
            total += tempValue;
        }
        return total;
    }

    /**
     * @param sumDatas
     * @param columnIndex
     * @return
     */
    private static double getMax(List<JSONObject> sumDatas, String columnIndex) {
        double max = sumDatas != null && sumDatas.size() > 0 ? sumDatas.get(0).getDouble(columnIndex) : 0;
        for (JSONObject data : sumDatas) {
            double tempValue = data.getDouble(columnIndex);
            if (tempValue > max) {
                max = tempValue;
            }
        }

        return max;
    }

    /**
     * @param sumDatas
     * @param columnIndex
     * @return
     */
    private static double getMin(List<JSONObject> sumDatas, String columnIndex) {
        double min = sumDatas != null && sumDatas.size() > 0 ? sumDatas.get(0).getDouble(columnIndex) : 0;
        for (JSONObject data : sumDatas) {
            double tempValue = data.getDouble(columnIndex);
            if (tempValue < min) {
                min = tempValue;
            }
        }
        return min;
    }

    /**
     * @param sumDatas
     * @param columnIndex
     * @return
     */
    private static double count(List<JSONObject> sumDatas, String columnIndex) {
        return sumDatas.size();
    }

    /**
     * @param sumDatas
     * @param columnIndex
     * @return
     */
    private static double countNoRepeat(List<JSONObject> sumDatas, String columnIndex) {
        Set<Double> counter = new HashSet<Double>();
        for (JSONObject data : sumDatas) {
            double tempValue = data.getDouble(columnIndex);
            counter.add(tempValue);
        }
        return counter.size();
    }

    /**
     * @param sumDatas
     * @param columnIndex
     * @return
     */
    private static double getAvg(List<JSONObject> sumDatas, String columnIndex) {
        double total = 0;
        for (JSONObject data : sumDatas) {
            double tempValue = data.getDouble(columnIndex);
            total += tempValue;
        }
        return total / sumDatas.size();
    }

    /**
     * @param sumDatas
     * @param columnIndex
     * @return
     */
    private static double getMiddle(List<JSONObject> sumDatas, final String columnIndex) {
        double middle = 0;
        Collections.sort(sumDatas, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                return o1.getDouble(columnIndex).compareTo(o2.getDouble(columnIndex));
            }
        });
        if (sumDatas.size() == 2) {
            middle = (sumDatas.get(0).getDouble(columnIndex) + sumDatas.get(1).getDouble(columnIndex)) / 2;
            return middle;
        }
        if (sumDatas.size() == 1) {
            middle = sumDatas.get(0).getDouble(columnIndex);
            return middle;
        }
        if (sumDatas.size() % 2 == 0) {
            middle = (sumDatas.get(sumDatas.size() / 2 - 1).getDouble(columnIndex) + sumDatas.get(sumDatas.size() / 2).getDouble(columnIndex)) / 2;
        } else {
            middle = sumDatas.get((sumDatas.size() + 1) / 2 - 1).getDouble(columnIndex);
        }
        return middle;
    }

    /**
     * 0
     * 1 升序
     * 2 降序
     * 3 求和
     * 4 最大值
     * 5 中位数
     * 6 平均值
     * 7 最小值
     * 8 计数
     * 9 计数不重复
     * [ [x,y] ,[]]
     */

    public static JSONArray getOneSeries(Map<String, List<JSONObject>> fixDatas, JSONObject oneYLable, List<JSONObject> sortDatas, String categoriesColumnIndex, JSONArray groupCategories) {
        int sort = oneYLable.getInteger("sort");
        JSONArray series = new JSONArray();
        final String columnIndex = oneYLable.getString("columnIndex");

        if (sort >= 3) {
            for (int catagoryIndex = 0; catagoryIndex < groupCategories.size(); catagoryIndex++) {
                String key = groupCategories.get(catagoryIndex).toString();
                JSONArray point = new JSONArray();
                point.add(key);
                List<JSONObject> sumDatas = fixDatas.get(key);
                if (sumDatas.size() > 0) {
                    if (sort == 3) {
                        point.add(getSum(sumDatas, columnIndex));
                    } else if (sort == 4) {
                        point.add(getMax(sumDatas, columnIndex));
                    } else if (sort == 5) {
                        point.add(getMiddle(sumDatas, columnIndex));
                    } else if (sort == 6) {
                        point.add(getAvg(sumDatas, columnIndex));
                    } else if (sort == 7) {
                        point.add(getMin(sumDatas, columnIndex));
                    } else if (sort == 8) {
                        point.add(count(sumDatas, columnIndex));
                    } else if (sort == 9) {
                        point.add(countNoRepeat(sumDatas, columnIndex));
                    }
                } else {
                    point.add(0);
                }
                series.add(point);
            }

        } else {
            //平铺数据
            for (int dataIndex = 0; dataIndex < sortDatas.size(); dataIndex++) {
                JSONArray point = new JSONArray();
                String key = sortDatas.get(dataIndex).getString(categoriesColumnIndex);
                point.add(key);
                point.add(sortDatas.get(dataIndex).getDouble(columnIndex));
                series.add(point);
            }
        }
        return series;
    }

    /**
     * 计算出 单个总数 / 所有的总数
     * data: [{
     * name: 'Chrome',
     * y: 61.41,
     * }]
     *
     * @param fixDatas
     * @param oneYLable
     * @param sortDatas
     * @param categoriesColumnIndex
     * @param groupCategories
     * @return
     */
    public static JSONArray getPieSeries(Map<String, List<JSONObject>> fixDatas, JSONObject oneYLable, List<JSONObject> sortDatas, String categoriesColumnIndex, JSONArray groupCategories) {
        JSONArray datas = getOneSeries(fixDatas, oneYLable, sortDatas, categoriesColumnIndex, groupCategories);
        TreeMap<String, Double> treeMapData = new TreeMap<String, Double>();
        double total = 0.0d;
        for (int index = 0; index < datas.size(); index++) {
            JSONArray point = datas.getJSONArray(index);
            String key = point.getString(0);
            double keyValue = point.getDoubleValue(1);
            total += keyValue;
            treeMapData.put(key, treeMapData.get(key) == null ? 0 + keyValue : treeMapData.get(key) + keyValue);
        }
        JSONArray result = new JSONArray();
        for (String key : treeMapData.keySet()) {
            JSONObject data = new JSONObject();
            data.put("name", key);
            double formateValue = formatDouble(treeMapData.get(key) / total);
            data.put("y", formateValue);
            data.put("value", treeMapData.get(key));
            result.add(data);
        }
        return result;
    }

    /**
     * 获取气泡图的 数据
     *
     * @param fixDatas
     * @param xLabel
     * @param yLabel
     * @param zLabel
     * @param groupCategories
     * @return
     */
    public static JSONArray getBubbleSeries(Map<String, List<JSONObject>> fixDatas, JSONArray xLabel, JSONArray yLabel, JSONArray zLabel, JSONArray groupCategories) {
        JSONArray series = new JSONArray();
        String xColumnIndex = xLabel.getJSONObject(0).getString("columnIndex");
        String yColumnIndex = yLabel.getJSONObject(0).getString("columnIndex");
        String zColumnIndex = zLabel.getJSONObject(0).getString("columnIndex");

        for (int index = 0; index < groupCategories.size(); index++) {
            JSONObject oneSeries = new JSONObject();
            JSONArray datas = new JSONArray();
            oneSeries.put("name", groupCategories.getString(index));
            List<JSONObject> dataList = fixDatas.get(groupCategories.getString(index));
            if (dataList != null && dataList.size() > 0) {
                for (int dataIndex = 0; dataIndex < dataList.size(); dataIndex++) {
                    JSONArray temp = new JSONArray();
                    temp.add(dataList.get(dataIndex).getDouble(xColumnIndex));
                    temp.add(dataList.get(dataIndex).getDouble(yColumnIndex));
                    temp.add(dataList.get(dataIndex).getDouble(zColumnIndex));
                    datas.add(temp);
                }
                oneSeries.put("data",datas);
            }
            if (datas != null && datas.size() > 0) {
                series.add(oneSeries);
            }
        }
        return series;
    }


    /**
     * 保留两位小数
     *
     * @param d
     * @return
     */
    public static double formatDouble(double d) {
        return (double) Math.round(d * 100) / 100;
    }

    /**
     * 是否为数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        str = str.trim();
        // 该正则表达式可以匹配所有的数字 包括负数
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;//异常 说明包含非数字。
        }

        Matcher isNum = pattern.matcher(bigStr); // matcher是全匹配
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 转换为数值
     *
     * @param value
     * @return
     */
    public static Number covertNumber(String value) {
        Number result = 0;
        try {
            result = Long.parseLong(value);
        } catch (Exception e) {
        }
        if (result.equals(0)) {
            try {
                result = Double.parseDouble(value);
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static void main(String[] args) {
        List<String> aa = new ArrayList<String>();
        aa.add("zhangrui1");
        aa.add("lisi");

       Iterator<String> it = aa.iterator();
       while(it.hasNext()){
           if(it.next().equals("zhangrui1")){
               it.remove();
           }
       }

       System.out.println(aa.size());
    }
}
