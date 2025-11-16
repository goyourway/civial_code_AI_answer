package com.jiing.demo.constants;

public class CivilCodeConstants {
    public static final String INDEX_NAME = "civil_code";

        public static final String MAPPING_TEMPLATE = "{\r\n" + //
              "  \"settings\": {\r\n" + //
              "    \"analysis\": {\r\n" + //
              "      \"filter\": {\r\n" + //
              "        \"length_filter\": {\r\n" + //
              "          \"type\": \"length\",\r\n" + //
              "          \"min\": 2\r\n" + //
              "        }\r\n" + //
              "      },\r\n" + //
              "      \"analyzer\": {\r\n" + //
              "        \"ik_max_word_filter\": {\r\n" + //
              "          \"type\": \"custom\",\r\n" + //
              "          \"tokenizer\": \"ik_max_word\",\r\n" + //
              "          \"filter\": [\"length_filter\"]\r\n" + //
              "        },\r\n" + //
              "        \"ik_smart_filter\": {\r\n" + //
              "          \"type\": \"custom\",\r\n" + //
              "          \"tokenizer\": \"ik_smart\",\r\n" + //
              "          \"filter\": [\"length_filter\"]\r\n" + //
              "        }\r\n" + //
              "      }\r\n" + //
              "    }\r\n" + //
              "  },\r\n" + //
              "  \"mappings\": {\r\n" + //
              "    \"properties\": {\r\n" + //
              "      \"code_content\": {\r\n" + //
              "        \"type\": \"text\",\r\n" + //
              "        \"analyzer\": \"ik_max_word_filter\",\r\n" + //
              "        \"search_analyzer\": \"ik_smart_filter\",\r\n" + //
              "        \"term_vector\": \"with_positions_offsets\"\r\n" + //
              "      }\r\n" + //
              "    }\r\n" + //
              "  }\r\n" + //
              "}";
}
