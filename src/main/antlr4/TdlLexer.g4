/**
 * Test Dynamic Language (TDL) grammar
 */

lexer grammar TdlLexer;

LineComment
    : '//' ~[\r\n]*
      -> channel(HIDDEN)
    ;

WS
    : [\u0020\u0009\u000C]
      -> channel(HIDDEN)
    ;

NL: '\n' | '\r' '\n'? ;

//SEPARATORS & OPERATIONS

DOT: '.' ;
COMMA: ',' ;
LPAREN: '(';
RPAREN: ')';
LCURL: '{';
RCURL: '}';
MULT: '*' ;
DIV: '/' ;
ADD: '+' ;
SUB: '-' ;
SEMICOLON: ';' ;
ASSIGNMENT: '=' ;
LANGLE: '<' ;
RANGLE: '>' ;

//KEYWORDS

IMPORT: 'import file' ;
TYPE: 'type' ;
FUNCTION: 'function' ;
INVOKE: 'invoke on' ;
IF: 'if' ;
RETURN: 'return' ;
AS: 'as' ;

QUOTE_OPEN: '"' -> pushMode(LineString) ;

fragment DecDigits
    : DecDigit DecDigit*
    | DecDigit
    ;

IntegerLiteral
    : DecDigitNoZero DecDigit*
    | DecDigit // including '0'
    ;

fragment DecDigit
    : '0'..'9'
    ;

fragment DecDigitNoZero
    : '1'..'9'
    ;

Identifier
    : Letter (Letter | DecDigit | '_' )*
    ;

fragment IdentifierOrSoftKey
    : Identifier //soft keywords:
    ;

fragment Letter
    : 'a'..'z'
    | 'A'..'Z'
    ;

ErrorCharacter: .;

mode LineString ;

QUOTE_CLOSE
    : '"' -> popMode
    ;

LineStrText
    : ~('\\' | '"' | '$')+ | '$'
    ;




