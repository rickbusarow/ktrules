/*
 * Copyright (C) 2025 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rickbusarow.ktrules.compat

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.parser.KDocElementTypes
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

/**
 * This is copy/pasted from KtLint. KtLint's version was moved
 * for 0.48.0 and again for 0.49.0. This version won't move.
 *
 * @since 1.1.1
 */
@Suppress("unused", "UndocumentedPublicProperty")
object ElementType {
  val FILE: IElementType = KtFileElementType.INSTANCE

  // KtNodeTypes
  val CLASS: IElementType = KtNodeTypes.CLASS
  val FUN: IElementType = KtNodeTypes.FUN
  val PROPERTY: IElementType = KtNodeTypes.PROPERTY
  val DESTRUCTURING_DECLARATION: IElementType = KtNodeTypes.DESTRUCTURING_DECLARATION
  val DESTRUCTURING_DECLARATION_ENTRY: IElementType = KtNodeTypes.DESTRUCTURING_DECLARATION_ENTRY
  val OBJECT_DECLARATION: IElementType = KtNodeTypes.OBJECT_DECLARATION
  val TYPEALIAS: IElementType = KtNodeTypes.TYPEALIAS
  val ENUM_ENTRY: IElementType = KtNodeTypes.ENUM_ENTRY
  val CLASS_INITIALIZER: IElementType = KtNodeTypes.CLASS_INITIALIZER
  val SCRIPT_INITIALIZER: IElementType = KtNodeTypes.SCRIPT_INITIALIZER
  val SECONDARY_CONSTRUCTOR: IElementType = KtNodeTypes.SECONDARY_CONSTRUCTOR
  val PRIMARY_CONSTRUCTOR: IElementType = KtNodeTypes.PRIMARY_CONSTRUCTOR
  val TYPE_PARAMETER_LIST: IElementType = KtNodeTypes.TYPE_PARAMETER_LIST
  val TYPE_PARAMETER: IElementType = KtNodeTypes.TYPE_PARAMETER
  val SUPER_TYPE_LIST: IElementType = KtNodeTypes.SUPER_TYPE_LIST
  val DELEGATED_SUPER_TYPE_ENTRY: IElementType = KtNodeTypes.DELEGATED_SUPER_TYPE_ENTRY
  val SUPER_TYPE_CALL_ENTRY: IElementType = KtNodeTypes.SUPER_TYPE_CALL_ENTRY
  val SUPER_TYPE_ENTRY: IElementType = KtNodeTypes.SUPER_TYPE_ENTRY
  val PROPERTY_DELEGATE: IElementType = KtNodeTypes.PROPERTY_DELEGATE
  val CONSTRUCTOR_CALLEE: IElementType = KtNodeTypes.CONSTRUCTOR_CALLEE
  val VALUE_PARAMETER_LIST: IElementType = KtNodeTypes.VALUE_PARAMETER_LIST
  val VALUE_PARAMETER: IElementType = KtNodeTypes.VALUE_PARAMETER
  val CLASS_BODY: IElementType = KtNodeTypes.CLASS_BODY
  val IMPORT_LIST: IElementType = KtNodeTypes.IMPORT_LIST
  val FILE_ANNOTATION_LIST: IElementType = KtNodeTypes.FILE_ANNOTATION_LIST
  val IMPORT_DIRECTIVE: IElementType = KtNodeTypes.IMPORT_DIRECTIVE
  val IMPORT_ALIAS: IElementType = KtNodeTypes.IMPORT_ALIAS
  val MODIFIER_LIST: IElementType = KtNodeTypes.MODIFIER_LIST
  val ANNOTATION: IElementType = KtNodeTypes.ANNOTATION
  val ANNOTATION_ENTRY: IElementType = KtNodeTypes.ANNOTATION_ENTRY
  val ANNOTATION_TARGET: IElementType = KtNodeTypes.ANNOTATION_TARGET
  val TYPE_ARGUMENT_LIST: IElementType = KtNodeTypes.TYPE_ARGUMENT_LIST
  val VALUE_ARGUMENT_LIST: IElementType = KtNodeTypes.VALUE_ARGUMENT_LIST
  val VALUE_ARGUMENT: IElementType = KtNodeTypes.VALUE_ARGUMENT
  val LAMBDA_ARGUMENT: IElementType = KtNodeTypes.LAMBDA_ARGUMENT
  val VALUE_ARGUMENT_NAME: IElementType = KtNodeTypes.VALUE_ARGUMENT_NAME
  val TYPE_REFERENCE: IElementType = KtNodeTypes.TYPE_REFERENCE
  val USER_TYPE: IElementType = KtNodeTypes.USER_TYPE
  val DYNAMIC_TYPE: IElementType = KtNodeTypes.DYNAMIC_TYPE
  val FUNCTION_TYPE: IElementType = KtNodeTypes.FUNCTION_TYPE
  val FUNCTION_TYPE_RECEIVER: IElementType = KtNodeTypes.FUNCTION_TYPE_RECEIVER
  val NULLABLE_TYPE: IElementType = KtNodeTypes.NULLABLE_TYPE
  val TYPE_PROJECTION: IElementType = KtNodeTypes.TYPE_PROJECTION
  val PROPERTY_ACCESSOR: IElementType = KtNodeTypes.PROPERTY_ACCESSOR
  val INITIALIZER_LIST: IElementType = KtNodeTypes.INITIALIZER_LIST
  val TYPE_CONSTRAINT_LIST: IElementType = KtNodeTypes.TYPE_CONSTRAINT_LIST
  val TYPE_CONSTRAINT: IElementType = KtNodeTypes.TYPE_CONSTRAINT
  val CONSTRUCTOR_DELEGATION_CALL: IElementType = KtNodeTypes.CONSTRUCTOR_DELEGATION_CALL
  val CONSTRUCTOR_DELEGATION_REFERENCE: IElementType = KtNodeTypes.CONSTRUCTOR_DELEGATION_REFERENCE
  val NULL: IElementType = KtNodeTypes.NULL
  val BOOLEAN_CONSTANT: IElementType = KtNodeTypes.BOOLEAN_CONSTANT
  val FLOAT_CONSTANT: IElementType = KtNodeTypes.FLOAT_CONSTANT
  val CHARACTER_CONSTANT: IElementType = KtNodeTypes.CHARACTER_CONSTANT
  val INTEGER_CONSTANT: IElementType = KtNodeTypes.INTEGER_CONSTANT
  val STRING_TEMPLATE: IElementType = KtNodeTypes.STRING_TEMPLATE
  val LONG_STRING_TEMPLATE_ENTRY: IElementType = KtNodeTypes.LONG_STRING_TEMPLATE_ENTRY
  val SHORT_STRING_TEMPLATE_ENTRY: IElementType = KtNodeTypes.SHORT_STRING_TEMPLATE_ENTRY
  val LITERAL_STRING_TEMPLATE_ENTRY: IElementType = KtNodeTypes.LITERAL_STRING_TEMPLATE_ENTRY
  val ESCAPE_STRING_TEMPLATE_ENTRY: IElementType = KtNodeTypes.ESCAPE_STRING_TEMPLATE_ENTRY
  val PARENTHESIZED: IElementType = KtNodeTypes.PARENTHESIZED
  val RETURN: IElementType = KtNodeTypes.RETURN
  val THROW: IElementType = KtNodeTypes.THROW
  val CONTINUE: IElementType = KtNodeTypes.CONTINUE
  val BREAK: IElementType = KtNodeTypes.BREAK
  val IF: IElementType = KtNodeTypes.IF
  val CONDITION: IElementType = KtNodeTypes.CONDITION
  val THEN: IElementType = KtNodeTypes.THEN
  val ELSE: IElementType = KtNodeTypes.ELSE
  val TRY: IElementType = KtNodeTypes.TRY
  val CATCH: IElementType = KtNodeTypes.CATCH
  val FINALLY: IElementType = KtNodeTypes.FINALLY
  val FOR: IElementType = KtNodeTypes.FOR
  val WHILE: IElementType = KtNodeTypes.WHILE
  val DO_WHILE: IElementType = KtNodeTypes.DO_WHILE
  val LOOP_RANGE: IElementType = KtNodeTypes.LOOP_RANGE
  val BODY: IElementType = KtNodeTypes.BODY
  val BLOCK: IElementType = KtNodeTypes.BLOCK
  val LAMBDA_EXPRESSION: IElementType = KtNodeTypes.LAMBDA_EXPRESSION
  val FUNCTION_LITERAL: IElementType = KtNodeTypes.FUNCTION_LITERAL
  val ANNOTATED_EXPRESSION: IElementType = KtNodeTypes.ANNOTATED_EXPRESSION
  val REFERENCE_EXPRESSION: IElementType = KtNodeTypes.REFERENCE_EXPRESSION
  val ENUM_ENTRY_SUPERCLASS_REFERENCE_EXPRESSION: IElementType =
    KtStubElementTypes.ENUM_ENTRY_SUPERCLASS_REFERENCE_EXPRESSION
  val OPERATION_REFERENCE: IElementType = KtNodeTypes.OPERATION_REFERENCE
  val LABEL: IElementType = KtNodeTypes.LABEL
  val LABEL_QUALIFIER: IElementType = KtNodeTypes.LABEL_QUALIFIER
  val THIS_EXPRESSION: IElementType = KtNodeTypes.THIS_EXPRESSION
  val SUPER_EXPRESSION: IElementType = KtNodeTypes.SUPER_EXPRESSION
  val BINARY_EXPRESSION: IElementType = KtNodeTypes.BINARY_EXPRESSION
  val BINARY_WITH_TYPE: IElementType = KtNodeTypes.BINARY_WITH_TYPE
  val IS_EXPRESSION: IElementType = KtNodeTypes.IS_EXPRESSION
  val PREFIX_EXPRESSION: IElementType = KtNodeTypes.PREFIX_EXPRESSION
  val POSTFIX_EXPRESSION: IElementType = KtNodeTypes.POSTFIX_EXPRESSION
  val LABELED_EXPRESSION: IElementType = KtNodeTypes.LABELED_EXPRESSION
  val CALL_EXPRESSION: IElementType = KtNodeTypes.CALL_EXPRESSION
  val ARRAY_ACCESS_EXPRESSION: IElementType = KtNodeTypes.ARRAY_ACCESS_EXPRESSION
  val INDICES: IElementType = KtNodeTypes.INDICES
  val DOT_QUALIFIED_EXPRESSION: IElementType = KtStubElementTypes.DOT_QUALIFIED_EXPRESSION
  val CALLABLE_REFERENCE_EXPRESSION: IElementType = KtNodeTypes.CALLABLE_REFERENCE_EXPRESSION
  val CLASS_LITERAL_EXPRESSION: IElementType = KtNodeTypes.CLASS_LITERAL_EXPRESSION
  val SAFE_ACCESS_EXPRESSION: IElementType = KtNodeTypes.SAFE_ACCESS_EXPRESSION
  val OBJECT_LITERAL: IElementType = KtNodeTypes.OBJECT_LITERAL
  val WHEN: IElementType = KtNodeTypes.WHEN
  val WHEN_ENTRY: IElementType = KtNodeTypes.WHEN_ENTRY
  val WHEN_CONDITION_IN_RANGE: IElementType = KtNodeTypes.WHEN_CONDITION_IN_RANGE
  val WHEN_CONDITION_IS_PATTERN: IElementType = KtNodeTypes.WHEN_CONDITION_IS_PATTERN
  val WHEN_CONDITION_WITH_EXPRESSION: IElementType = KtNodeTypes.WHEN_CONDITION_EXPRESSION
  val COLLECTION_LITERAL_EXPRESSION: IElementType = KtNodeTypes.COLLECTION_LITERAL_EXPRESSION
  val PACKAGE_DIRECTIVE: IElementType = KtNodeTypes.PACKAGE_DIRECTIVE
  val SCRIPT: IElementType = KtNodeTypes.SCRIPT
  val TYPE_CODE_FRAGMENT: IElementType = KtNodeTypes.TYPE_CODE_FRAGMENT
  val EXPRESSION_CODE_FRAGMENT: IElementType = KtNodeTypes.EXPRESSION_CODE_FRAGMENT
  val BLOCK_CODE_FRAGMENT: IElementType = KtNodeTypes.BLOCK_CODE_FRAGMENT
  val CONTEXT_RECEIVER_LIST: IElementType = KtNodeTypes.CONTEXT_RECEIVER_LIST
  val CONTEXT_RECEIVER: IElementType = KtNodeTypes.CONTEXT_RECEIVER

  // KtTokens
  val EOF: IElementType = KtTokens.EOF
  val RESERVED: IElementType = KtTokens.RESERVED
  val BLOCK_COMMENT: IElementType = KtTokens.BLOCK_COMMENT
  val EOL_COMMENT: IElementType = KtTokens.EOL_COMMENT
  val SHEBANG_COMMENT: IElementType = KtTokens.SHEBANG_COMMENT
  val WHITE_SPACE: IElementType = KtTokens.WHITE_SPACE
  val INTEGER_LITERAL: IElementType = KtTokens.INTEGER_LITERAL
  val FLOAT_LITERAL: IElementType = KtTokens.FLOAT_LITERAL // FLOAT_CONSTANT
  val CHARACTER_LITERAL: IElementType = KtTokens.CHARACTER_LITERAL
  val CLOSING_QUOTE: IElementType = KtTokens.CLOSING_QUOTE
  val OPEN_QUOTE: IElementType = KtTokens.OPEN_QUOTE
  val REGULAR_STRING_PART: IElementType = KtTokens.REGULAR_STRING_PART
  val ESCAPE_SEQUENCE: IElementType = KtTokens.ESCAPE_SEQUENCE
  val SHORT_TEMPLATE_ENTRY_START: IElementType = KtTokens.SHORT_TEMPLATE_ENTRY_START
  val LONG_TEMPLATE_ENTRY_START: IElementType = KtTokens.LONG_TEMPLATE_ENTRY_START
  val LONG_TEMPLATE_ENTRY_END: IElementType = KtTokens.LONG_TEMPLATE_ENTRY_END
  val DANGLING_NEWLINE: IElementType = KtTokens.DANGLING_NEWLINE
  val PACKAGE_KEYWORD: IElementType = KtTokens.PACKAGE_KEYWORD
  val AS_KEYWORD: IElementType = KtTokens.AS_KEYWORD
  val TYPEALIAS_KEYWORD: IElementType = KtTokens.TYPE_ALIAS_KEYWORD
  val CLASS_KEYWORD: IElementType = KtTokens.CLASS_KEYWORD
  val THIS_KEYWORD: IElementType = KtTokens.THIS_KEYWORD
  val SUPER_KEYWORD: IElementType = KtTokens.SUPER_KEYWORD
  val VAL_KEYWORD: IElementType = KtTokens.VAL_KEYWORD
  val VAR_KEYWORD: IElementType = KtTokens.VAR_KEYWORD
  val FUN_KEYWORD: IElementType = KtTokens.FUN_KEYWORD
  val FOR_KEYWORD: IElementType = KtTokens.FOR_KEYWORD
  val NULL_KEYWORD: IElementType = KtTokens.NULL_KEYWORD
  val TRUE_KEYWORD: IElementType = KtTokens.TRUE_KEYWORD
  val FALSE_KEYWORD: IElementType = KtTokens.FALSE_KEYWORD
  val IS_KEYWORD: IElementType = KtTokens.IS_KEYWORD
  val IN_KEYWORD: IElementType = KtTokens.IN_KEYWORD
  val THROW_KEYWORD: IElementType = KtTokens.THROW_KEYWORD
  val RETURN_KEYWORD: IElementType = KtTokens.RETURN_KEYWORD
  val BREAK_KEYWORD: IElementType = KtTokens.BREAK_KEYWORD
  val CONTINUE_KEYWORD: IElementType = KtTokens.CONTINUE_KEYWORD
  val OBJECT_KEYWORD: IElementType = KtTokens.OBJECT_KEYWORD
  val IF_KEYWORD: IElementType = KtTokens.IF_KEYWORD
  val TRY_KEYWORD: IElementType = KtTokens.TRY_KEYWORD
  val ELSE_KEYWORD: IElementType = KtTokens.ELSE_KEYWORD
  val WHILE_KEYWORD: IElementType = KtTokens.WHILE_KEYWORD
  val DO_KEYWORD: IElementType = KtTokens.DO_KEYWORD
  val WHEN_KEYWORD: IElementType = KtTokens.WHEN_KEYWORD
  val INTERFACE_KEYWORD: IElementType = KtTokens.INTERFACE_KEYWORD
  val TYPEOF_KEYWORD: IElementType = KtTokens.TYPEOF_KEYWORD
  val AS_SAFE: IElementType = KtTokens.AS_SAFE
  val IDENTIFIER: IElementType = KtTokens.IDENTIFIER
  val FIELD_IDENTIFIER: IElementType = KtTokens.FIELD_IDENTIFIER
  val LBRACKET: IElementType = KtTokens.LBRACKET
  val RBRACKET: IElementType = KtTokens.RBRACKET
  val LBRACE: IElementType = KtTokens.LBRACE
  val RBRACE: IElementType = KtTokens.RBRACE
  val LPAR: IElementType = KtTokens.LPAR
  val RPAR: IElementType = KtTokens.RPAR
  val DOT: IElementType = KtTokens.DOT
  val PLUSPLUS: IElementType = KtTokens.PLUSPLUS
  val MINUSMINUS: IElementType = KtTokens.MINUSMINUS
  val MUL: IElementType = KtTokens.MUL
  val PLUS: IElementType = KtTokens.PLUS
  val MINUS: IElementType = KtTokens.MINUS
  val EXCL: IElementType = KtTokens.EXCL
  val DIV: IElementType = KtTokens.DIV
  val PERC: IElementType = KtTokens.PERC
  val LT: IElementType = KtTokens.LT
  val GT: IElementType = KtTokens.GT
  val LTEQ: IElementType = KtTokens.LTEQ
  val GTEQ: IElementType = KtTokens.GTEQ
  val EQEQEQ: IElementType = KtTokens.EQEQEQ
  val ARROW: IElementType = KtTokens.ARROW
  val DOUBLE_ARROW: IElementType = KtTokens.DOUBLE_ARROW
  val EXCLEQEQEQ: IElementType = KtTokens.EXCLEQEQEQ
  val EQEQ: IElementType = KtTokens.EQEQ
  val EXCLEQ: IElementType = KtTokens.EXCLEQ
  val EXCLEXCL: IElementType = KtTokens.EXCLEXCL
  val ANDAND: IElementType = KtTokens.ANDAND
  val OROR: IElementType = KtTokens.OROR
  val SAFE_ACCESS: IElementType = KtTokens.SAFE_ACCESS
  val ELVIS: IElementType = KtTokens.ELVIS
  val QUEST: IElementType = KtTokens.QUEST
  val COLONCOLON: IElementType = KtTokens.COLONCOLON
  val COLON: IElementType = KtTokens.COLON
  val SEMICOLON: IElementType = KtTokens.SEMICOLON
  val DOUBLE_SEMICOLON: IElementType = KtTokens.DOUBLE_SEMICOLON
  val RANGE: IElementType = KtTokens.RANGE
  val RANGE_UNTIL: IElementType = KtTokens.RANGE_UNTIL
  val EQ: IElementType = KtTokens.EQ
  val MULTEQ: IElementType = KtTokens.MULTEQ
  val DIVEQ: IElementType = KtTokens.DIVEQ
  val PERCEQ: IElementType = KtTokens.PERCEQ
  val PLUSEQ: IElementType = KtTokens.PLUSEQ
  val MINUSEQ: IElementType = KtTokens.MINUSEQ
  val NOT_IN: IElementType = KtTokens.NOT_IN
  val NOT_IS: IElementType = KtTokens.NOT_IS
  val HASH: IElementType = KtTokens.HASH
  val AT: IElementType = KtTokens.AT
  val COMMA: IElementType = KtTokens.COMMA
  val EOL_OR_SEMICOLON: IElementType = KtTokens.EOL_OR_SEMICOLON
  val FILE_KEYWORD: IElementType = KtTokens.FILE_KEYWORD
  val FIELD_KEYWORD: IElementType = KtTokens.FIELD_KEYWORD
  val PROPERTY_KEYWORD: IElementType = KtTokens.PROPERTY_KEYWORD
  val RECEIVER_KEYWORD: IElementType = KtTokens.RECEIVER_KEYWORD
  val PARAM_KEYWORD: IElementType = KtTokens.PARAM_KEYWORD
  val SETPARAM_KEYWORD: IElementType = KtTokens.SETPARAM_KEYWORD
  val DELEGATE_KEYWORD: IElementType = KtTokens.DELEGATE_KEYWORD
  val IMPORT_KEYWORD: IElementType = KtTokens.IMPORT_KEYWORD
  val WHERE_KEYWORD: IElementType = KtTokens.WHERE_KEYWORD
  val BY_KEYWORD: IElementType = KtTokens.BY_KEYWORD
  val GET_KEYWORD: IElementType = KtTokens.GET_KEYWORD
  val SET_KEYWORD: IElementType = KtTokens.SET_KEYWORD
  val CONSTRUCTOR_KEYWORD: IElementType = KtTokens.CONSTRUCTOR_KEYWORD
  val INIT_KEYWORD: IElementType = KtTokens.INIT_KEYWORD
  val ABSTRACT_KEYWORD: IElementType = KtTokens.ABSTRACT_KEYWORD
  val ENUM_KEYWORD: IElementType = KtTokens.ENUM_KEYWORD
  val OPEN_KEYWORD: IElementType = KtTokens.OPEN_KEYWORD
  val INNER_KEYWORD: IElementType = KtTokens.INNER_KEYWORD
  val OVERRIDE_KEYWORD: IElementType = KtTokens.OVERRIDE_KEYWORD
  val PRIVATE_KEYWORD: IElementType = KtTokens.PRIVATE_KEYWORD
  val PUBLIC_KEYWORD: IElementType = KtTokens.PUBLIC_KEYWORD
  val INTERNAL_KEYWORD: IElementType = KtTokens.INTERNAL_KEYWORD
  val PROTECTED_KEYWORD: IElementType = KtTokens.PROTECTED_KEYWORD
  val CATCH_KEYWORD: IElementType = KtTokens.CATCH_KEYWORD
  val OUT_KEYWORD: IElementType = KtTokens.OUT_KEYWORD
  val VARARG_KEYWORD: IElementType = KtTokens.VARARG_KEYWORD
  val REIFIED_KEYWORD: IElementType = KtTokens.REIFIED_KEYWORD
  val DYNAMIC_KEYWORD: IElementType = KtTokens.DYNAMIC_KEYWORD
  val COMPANION_KEYWORD: IElementType = KtTokens.COMPANION_KEYWORD
  val SEALED_KEYWORD: IElementType = KtTokens.SEALED_KEYWORD
  val DEFAULT_VISIBILITY_KEYWORD: IElementType = PUBLIC_KEYWORD
  val FINALLY_KEYWORD: IElementType = KtTokens.FINALLY_KEYWORD
  val FINAL_KEYWORD: IElementType = KtTokens.FINAL_KEYWORD
  val LATEINIT_KEYWORD: IElementType = KtTokens.LATEINIT_KEYWORD
  val DATA_KEYWORD: IElementType = KtTokens.DATA_KEYWORD
  val INLINE_KEYWORD: IElementType = KtTokens.INLINE_KEYWORD
  val NOINLINE_KEYWORD: IElementType = KtTokens.NOINLINE_KEYWORD
  val TAILREC_KEYWORD: IElementType = KtTokens.TAILREC_KEYWORD
  val EXTERNAL_KEYWORD: IElementType = KtTokens.EXTERNAL_KEYWORD
  val ANNOTATION_KEYWORD: IElementType = KtTokens.ANNOTATION_KEYWORD
  val CROSSINLINE_KEYWORD: IElementType = KtTokens.CROSSINLINE_KEYWORD
  val OPERATOR_KEYWORD: IElementType = KtTokens.OPERATOR_KEYWORD
  val INFIX_KEYWORD: IElementType = KtTokens.INFIX_KEYWORD
  val CONST_KEYWORD: IElementType = KtTokens.CONST_KEYWORD
  val SUSPEND_KEYWORD: IElementType = KtTokens.SUSPEND_KEYWORD

  val EXPECT_KEYWORD: IElementType = KtTokens.EXPECT_KEYWORD
  val ACTUAL_KEYWORD: IElementType = KtTokens.ACTUAL_KEYWORD

  // KDocTokens
  val KDOC: IElementType = KDocTokens.KDOC
  val KDOC_START: IElementType = KDocTokens.START
  val KDOC_END: IElementType = KDocTokens.END
  val KDOC_LEADING_ASTERISK: IElementType = KDocTokens.LEADING_ASTERISK
  val KDOC_TEXT: IElementType = KDocTokens.TEXT
  val KDOC_CODE_BLOCK_TEXT: IElementType = KDocTokens.CODE_BLOCK_TEXT
  val KDOC_TAG_NAME: IElementType = KDocTokens.TAG_NAME
  val KDOC_MARKDOWN_LINK: IElementType = KDocTokens.MARKDOWN_LINK
  val KDOC_MARKDOWN_ESCAPED_CHAR: IElementType = KDocTokens.MARKDOWN_ESCAPED_CHAR
  val KDOC_MARKDOWN_INLINE_LINK: IElementType = KDocTokens.MARKDOWN_INLINE_LINK
  val KDOC_SECTION: IElementType = KDocElementTypes.KDOC_SECTION
  val KDOC_TAG: IElementType = KDocElementTypes.KDOC_TAG
  val KDOC_NAME: IElementType = KDocElementTypes.KDOC_NAME
}
