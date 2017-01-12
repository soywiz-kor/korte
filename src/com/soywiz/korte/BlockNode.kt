package com.soywiz.korte

import com.soywiz.korio.error.invalidOp
import com.soywiz.korio.util.ListReader
import com.soywiz.korte.block.BlockExpr
import com.soywiz.korte.block.BlockGroup
import com.soywiz.korte.block.BlockText
import com.soywiz.korte.tag.TagEmpty

interface BlockNode {
	fun eval(context: Template.Context): Unit

	companion object {
		fun group(children: List<BlockNode>): BlockNode = if (children.size == 1) children[0] else BlockGroup(children)

		fun parse(tokens: List<Token>, config: Template.Config): BlockNode {
			val tr = ListReader(tokens)
			fun handle(tag: Tag, token: Token.TTag): BlockNode {
				val parts = arrayListOf<Tag.Part>()
				var currentToken = token
				val children = arrayListOf<BlockNode>()

				fun emitPart() {
					parts += Tag.Part(currentToken, group(children))
				}

				loop@ while (!tr.eof) {
					val it = tr.read()
					when (it) {
						is Token.TLiteral -> children += BlockText(it.content)
						is Token.TExpr -> children += BlockExpr(ExprNode.parse(it.content))
						is Token.TTag -> {
							when (it.name) {
								tag.end -> break@loop
								in tag.nextList -> {
									emitPart()
									currentToken = it
									children.clear()
								}
								else -> {
									val newtag = config.tags[it.name] ?: invalidOp("Can't find tag ${it.name}")
									if (newtag.end != null) {
										children += handle(newtag, it)
									} else {
										children += newtag.buildNode(listOf(Tag.Part(it, BlockText(""))))
									}
								}
							}
						}
						else -> break@loop
					}
				}

				emitPart()

				return tag.buildNode(parts)
			}
			return handle(TagEmpty, Token.TTag("", ""))
		}
	}
}
