package com.jiing.demo.thirdparty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileRegexMatcher {

     // 文件路径
    private static String filePath = "src\\main\\resources\\static\\minfadian.txt"; 
    private static String outputFilePath = "src\\main\\resources\\static\\extracted_articles.txt"; // 输出文件路径

    public static void main(String[] args) {
        

        try {
            List<String> articles = readCivilCode();
            // 输出提取的条目到文件
            StringBuilder outputContent = new StringBuilder();
            for (String article : articles) {
                outputContent.append(article.trim());
            }
            // 写入到新文件
            Files.write(Paths.get(outputFilePath), outputContent.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            //System.out.println("提取的条目已写入到文件：" + outputFilePath);
        } catch (IOException e) {
            System.err.println("文件读取失败: " + e.getMessage());
        }
    }

    public static List<String> readCivilCode(){
        // 存储提取的条目
        List<String> articles = new ArrayList<>();
        try {
            // 读取文件内容为字符串
            String content = new String(Files.readAllBytes(Paths.get(filePath)),StandardCharsets.UTF_8);

            // 正则表达式匹配“第xx条”及其后内容
            String regex = "第[一二三四五六七八九十百千零]+条.*?(?=(第[一二三四五六七八九十百千零]+条|第[一二三四五六七八九十百千零]+章|$))";

            // 编译正则表达式
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            // 提取匹配内容
            while (matcher.find()) {
                articles.add(matcher.group().trim());
            }
        } catch (IOException e) {
            System.err.println("文件读取失败: " + e.getMessage());
        }
        return articles;
    }
}