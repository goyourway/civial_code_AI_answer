package com.jiing.demo.constants;

public class LawCaseConstants {
   public static final String INDEX_NAME = "law_case";

    public static final String MAPPING_TEMPLATE = "{\r\n" + //
              "  \"mappings\": {\r\n" + //
              "    \"properties\": {\r\n" + //
              "      \"name\": {\r\n" + //
              "        \"type\": \"text\",\r\n" + //
              "        \"analyzer\": \"ik_max_word\",\r\n" + //
              "        \"copy_to\": \"all\"\r\n" + //
              "      },\r\n" + //
              "      \"content\": {\r\n" + //
              "        \"type\": \"text\",\r\n" + //
              "        \"analyzer\": \"ik_max_word\",\r\n" + //
              "        \"copy_to\": \"all\"\r\n" + //
              "      },\r\n" + //
              "      \"date\": {\r\n" + //
              "        \"type\": \"date\"\r\n" + //
              "      },\r\n" + //
              "      \"lawType\": {\r\n" + //
              "        \"type\": \"keyword\"\r\n" + //
              "      },\r\n" + //
              "      \"all\": {\r\n" + //
              "        \"type\": \"text\",\r\n" + //
              "        \"analyzer\": \"ik_max_word\"\r\n" + //
              "      }\r\n" + //
              "    }\r\n" + //
              "  }\r\n" + //
              "}";
}
