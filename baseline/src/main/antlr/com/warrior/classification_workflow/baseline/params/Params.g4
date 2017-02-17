grammar Params;

@header {
    package com.warrior.classification_workflow.baseline.params;
}

expression
    :   primary
    |   function
    |   unaryMinus
    |   left=expression op=('*' | '/') right=expression
    |   left=expression op=('+' | '-') right=expression
    ;

primary
    :   '(' expression ')'
    |   doubleLiteral
    |   numInstances
    |   numAttributes
    |   start
    |   end
    ;

unaryMinus
    :   '-' expression
    ;

function
    :   fun=SQRT '(' expression ')'
    |   fun=ROUND '(' expression ')'
    ;

doubleLiteral
    :   DoubleLiteral
    ;

numInstances
    :   NumInstances
    ;

numAttributes
    :   NumAttributes
    ;

start
    :   Start
    ;

end
    :   End
    ;

//
//  Lexer
//

DoubleLiteral
    :   Digits ('.' Digits?)?
    ;

fragment
Digits
    :   [0-9]+
    ;

NumInstances    : 'instances';
NumAttributes   : 'attributes';
Start           : 'start';
End             : 'end';

//
//  Functions
//
SQRT            : 'sqrt';
ROUND           : 'round';

//
//  Operators
//
ADD             : '+';
SUB             : '-';
MUL             : '*';
DIV             : '/';

//
//  Whitespace and comments
//
WS
    :  [ \t\r\n]+ -> skip
    ;
