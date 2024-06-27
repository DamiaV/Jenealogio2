grammar gedcom;

DELIM: ' ';
TERMINATOR: '\r' | '\n' | '\r\n';
LEVEL: '0' | [1-9][0-9]?;
AT: '@';
ALPHA: [A-Za-z];
DIGIT: [0-9];
ALPHANUM: ALPHA | DIGIT;
CHAR: [^\u0000-\u0008\u000a-\u001f@\u00ff];
HASH: '#';

gedcom_line: LEVEL (DELIM xref_id)? DELIM tag (DELIM line_value)? TERMINATOR;

xref_id: AT identifier_string AT;

identifier_string: ALPHANUM+;

tag: '_'? ALPHANUM
   | tag ALPHANUM
   ;

line_value: xref_id
          | line_item
          ;

line_item: (escape DELIM)? line_text;

line_text: line_char+;

line_char: AT AT
         | CHAR
         ;

escape: AT HASH escape_text AT;

escape_text: ALPHANUM
           | escape_text ALPHANUM
           ;
