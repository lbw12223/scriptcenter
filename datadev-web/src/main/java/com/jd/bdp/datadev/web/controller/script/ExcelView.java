package com.jd.bdp.datadev.web.controller.script;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class ExcelView extends AbstractView {
    /**
     * The content type for an Excel response
     */
    private static final String contentType = "applicatoin/octet-stream";

    /**
     * The extension to look for existing templates
     */
    private static final String EXTENSION = ".xlsx";
    private static final String DEFAULT_FILE_NAME="script.xlsx";

    private List<String> titleList;
    private List<List<String>>dataList;
    private String fileName;


    public ExcelView(List<String> titleList, List<List<String>> dataList,String fileName){
        this.titleList=titleList;
        this.dataList =dataList;
        this.fileName=fileName;
    }

    protected void buildExcelDocument(Map<String, Object> model, XSSFWorkbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        XSSFSheet sheet = workbook.createSheet("list");
        sheet.setDefaultColumnWidth(30);

        //设置列名
        XSSFRow titleRow = sheet.createRow(0);
        Integer titleNum=0;
        for(String titItem:titleList){
            XSSFCell cell=titleRow.createCell(titleNum++);
            cell.setCellType(XSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(titItem);
        }

        //插入数据
        for(int rowIndex=1;rowIndex<=dataList.size();rowIndex++){
            List<String> dataRowList = dataList.get(rowIndex-1);
            XSSFRow dataRow = sheet.createRow(rowIndex);
            for(int cellIndex=0;cellIndex<dataRowList.size();cellIndex++){
                XSSFCell cell=dataRow.createCell(cellIndex);
                cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                cell.setCellValue(dataRowList.get(cellIndex));
            }
        }
    }

    protected void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if(StringUtils.isNotBlank(fileName)){
            fileName=fileName+EXTENSION;
        }else {
            fileName=DEFAULT_FILE_NAME;
        }
        response.setHeader("Content-disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode(fileName,"UTF-8"));
        XSSFWorkbook workbook = new XSSFWorkbook();

        buildExcelDocument(model, workbook, request, response);

        // Set the content type.
        response.setContentType(contentType);
        // Should we set the content length here?
        // response.setContentLength(workbook.getBytes().length);

        // Flush byte array to servlet output stream.
        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        out.flush();
    }
}
