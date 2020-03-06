/**
 * Test Dynamic Language (TDL) grammar
 */

parser grammar TdlParser;

options { tokenVocab = TdlLexer; }

tdlFile
    : NL* importList topLevelObject* EOF
    ;

importList
    : importHeader*
    ;

importHeader
    : IMPORT simpleIdentifier semi
    ;

topLevelObject
    : declaration semi
    ;

typeDeclaration
    : TYPE NL* simpleIdentifier NL* primaryConstructor
    ;

primaryConstructor
    : parameters
    ;

parameters
    : LPAREN NL* (parameter (NL* COMMA NL* parameter)*)? NL* RPAREN
    ;

functionDeclaration
    : FUNCTION
    (NL* simpleIdentifier)
    NL* parameters
    (NL* functionBody)?
    ;

invokeOnDeclaration
    : INVOKE
    (NL* simpleIdentifier)
    (NL* functionBody)
    ;

parameter
    : simpleIdentifier NL*
    ;

functionBody
    : block
    ;

type
    : typeReference
    ;


typeReference
    : userType
    ;

userType
    : simpleUserType
    ;

simpleUserType
    : simpleIdentifier
    ;

block
    : LCURL NL* statements NL* RCURL
    ;

statements
    : (statement (semi statement)* semi)?
    ;

statement
    :
    assignment
    | ifStatement
    | jumpStatement
    | expression
    ;

declaration
    : typeDeclaration
    | functionDeclaration
    | invokeOnDeclaration
    | assignment
    ;

assignment
    : assignableExpression ASSIGNMENT NL* expression
    ;

expression
    : asExpression
    | comparison
    ;

asExpression
    : additiveExpression NL* asOperator NL* type
    ;

comparison
    : additiveExpression (comparisonOperator NL* additiveExpression)?
    ;

additiveExpression
    : multiplicativeExpression (additiveOperator NL* multiplicativeExpression)*
    ;

multiplicativeExpression
    : expressionWithSuffix (multiplicativeOperator NL* expressionWithSuffix)*
    ;

expressionWithSuffix
    : primaryExpression (suffix)*
    ;

suffix
    : callSuffix
    | navigationSuffix
    ;

assignableExpression
    : simpleIdentifier
    ;

navigationSuffix
    : NL* memberAccessOperator NL* simpleIdentifier
    ;

callSuffix
    : valueArguments
    ;

valueArguments
    : LPAREN RPAREN
    | LPAREN NL* valueArgument (NL* COMMA NL* valueArgument)* NL* RPAREN
    ;

valueArgument
    : NL* expression
    ;

primaryExpression
    : parenthesizedExpression
    | literalConstant
    | stringLiteral
    | simpleIdentifier
    ;

parenthesizedExpression
    : LPAREN NL* expression NL* RPAREN
    ;

literalConstant
    : IntegerLiteral
    ;

stringLiteral
    : lineStringLiteral
    ;

lineStringLiteral
    : QUOTE_OPEN (lineStringContent)* QUOTE_CLOSE
    ;

lineStringContent
    : LineStrText
    ;

ifStatement
    : IF NL* LPAREN NL* expression NL* RPAREN NL* block
    ;

jumpStatement
    : RETURN NL* expression?
    ;

comparisonOperator
    : LANGLE
    | RANGLE
    ;

additiveOperator
    : ADD | SUB
    ;

multiplicativeOperator
    : MULT | DIV
    ;

asOperator
    : AS
    ;

memberAccessOperator
    : DOT
    ;

simpleIdentifier
    : Identifier
    ;

semi
    : SEMICOLON NL*
    | EOF;