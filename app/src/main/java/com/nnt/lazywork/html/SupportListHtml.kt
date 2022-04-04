package com.nnt.lazywork.html

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.text.*
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import android.util.Log
import com.nnt.lazywork.util.convertDpToPixel
import org.xml.sax.XMLReader
import java.util.*

/**
 * String.fromHtml() does not support list <ul> <ol> tag, so if you want show these tabs, you can use this function
 */
fun fromSupportListTagHtml(
    context: Context,
    html: String,
    bulletRadiusDp: Float = 2.5F,
    marginStartDp: Float = 15F
): SpannableStringBuilder {
    val marginStartPx = convertDpToPixel(marginStartDp, context).toInt()
    val bulletRadiusPx = convertDpToPixel(bulletRadiusDp, context).toInt()
    val liTagHandler = LiTagHandler(bulletRadius = bulletRadiusPx, marginStart = marginStartPx)

    @Suppress("DEPRECATION")
    val htmlSpannable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(customizeListTags(html), Html.FROM_HTML_MODE_LEGACY, null, liTagHandler)
    } else {
        Html.fromHtml(html, null, liTagHandler)
    }
    return SpannableStringBuilder(htmlSpannable.trim())
}

private fun customizeListTags(html: String): String {
    var newHtml = html
    newHtml = newHtml.replace("<ul", "<SUL")
    newHtml = newHtml.replace("</ul>", "</SUL>")
    newHtml = newHtml.replace("<ol", "<SOL")
    newHtml = newHtml.replace("</ol>", "</SOL>")
    newHtml = newHtml.replace("<li", "<SLI")
    newHtml = newHtml.replace("</li>", "</SLI>")
    return newHtml
}

private class LiTagHandler(private val bulletRadius: Int=6, private val marginStart: Int= 20) : Html.TagHandler {
    private val lists: Stack<ListTag> = Stack()
    override fun handleTag(
        opening: Boolean,
        tag: String,
        output: Editable,
        xmlReader: XMLReader
    ) {
        if (UL_TAG.equals(tag, ignoreCase = true)) {
            if (opening) {
                lists.push(Ul(bulletRadius =  bulletRadius, marginStart = marginStart))
            } else {
                lists.pop()
            }
        } else if (OL_TAG.equals(tag, ignoreCase = true)) {
            if (opening) {
                lists.push(Ol(marginStart= marginStart)) // use default start index of 1
            } else {
                lists.pop()
            }
        } else if (LI_TAG.equals(tag, ignoreCase = true)) {
            if (opening) {
                lists.peek().openItem(output)
            } else {
                lists.peek().closeItem(output, lists.size)
            }
        } else {
            Log.d("TagHandler", "Found an unsupported tag $tag")
        }
    }

    /**
     * Abstract super class for [Ul] and [Ol].
     */
    private abstract class ListTag {
        /**
         * Opens a new list item.
         *
         * @param text
         */
        open fun openItem(text: Editable) {
            if (text.length > 0 && text[text.length - 1] != '\n') {
                text.append("\n")
            }
            val len = text.length
            text.setSpan(this, len, len, Spanned.SPAN_MARK_MARK)
        }

        /**
         * Closes a list item.
         *
         * @param text
         * @param indentation
         */
        fun closeItem(text: Editable, indentation: Int) {
            if (text.length > 0 && text[text.length - 1] != '\n') {
                text.append("\n")
            }
            val replaces = getReplaces(text, indentation)
            val len = text.length
            val listTag = getLast(text)
            val where = text.getSpanStart(listTag)
            text.removeSpan(listTag)
            if (where != len) {
                for (replace in replaces) {
                    text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        protected abstract fun getReplaces(text: Editable?, indentation: Int): Array<Any>

        /**
         * Note: This knows that the last returned object from getSpans() will be the most recently added.
         *
         * @see Html
         */
        private fun getLast(text: Spanned): ListTag? {
            val listTags = text.getSpans(
                0, text.length,
                ListTag::class.java
            )
            return if (listTags.size == 0) {
                null
            } else listTags[listTags.size - 1]
        }
    }

    /**
     * Class representing the unordered list (`<ul>`) HTML tag.
     */
    private class Ul(private val bulletRadius: Int, private val marginStart: Int ) : ListTag() {
        override fun getReplaces(text: Editable?, indentation: Int): Array<Any> {
            // Nested BulletSpans increases distance between BULLET_SPAN and text, so we must prevent it.
            var bulletMargin = INDENT_PX
            if (indentation > 1) {
                bulletMargin = INDENT_PX - BULLET_SPAN.getLeadingMargin(true)
                if (indentation > 2) {
                    // This get's more complicated when we add a LeadingMarginSpan into the same line:
                    // we have also counter it's effect to BulletSpan
                    bulletMargin -= (indentation - 2) * LIST_ITEM_INDENT_PX
                }
            }
            return arrayOf(
                LeadingMarginSpan.Standard(LIST_ITEM_INDENT_PX * (indentation - 1)),
                ImprovedBulletSpan(gapWidth = bulletMargin, bulletRadius = bulletRadius, marginStart = marginStart)
            )
        }
    }

    /**
     * Class representing the ordered list (`<ol>`) HTML tag.
     */
    private class Ol
    /**
     * Creates a new `<ol>` with start index of 1.
     */ @JvmOverloads constructor(private var nextIdx: Int = 1, private val marginStart: Int=20) : ListTag() {
        override fun openItem(text: Editable) {
            super.openItem(text)
            text.append(Integer.toString(nextIdx++)).append(". ")
        }

        override fun getReplaces(text: Editable?, indentation: Int): Array<Any> {
            var numberMargin = LIST_ITEM_INDENT_PX * (indentation - 1)
            if (indentation > 2) {
                // Same as in ordered lists: counter the effect of nested Spans
                numberMargin -= (indentation - 2) * LIST_ITEM_INDENT_PX
            }
            return arrayOf(LeadingMarginSpan.Standard(numberMargin + marginStart))
        }
        /**
         * Creates a new `<ul>` with given start index.
         *
         * @param nextIdx
         */
    }

    companion object {
        private const val OL_TAG = "sol"
        private const val UL_TAG = "sul"
        private const val LI_TAG = "sli"
        private const val INDENT_PX = 15
        private const val LIST_ITEM_INDENT_PX = INDENT_PX * 2
        private val BULLET_SPAN = BulletSpan(INDENT_PX)
    }
}

private class ImprovedBulletSpan(
    val bulletRadius: Int = STANDARD_BULLET_RADIUS,
    val marginStart: Int = STANDARD_MARGIN_START,
    val gapWidth: Int = STANDARD_GAP_WIDTH,
    val color: Int = STANDARD_COLOR
) : LeadingMarginSpan {

    companion object {
        // Bullet is slightly bigger to avoid aliasing artifacts on mdpi devices.
        private const val STANDARD_BULLET_RADIUS = 6
        private const val STANDARD_GAP_WIDTH = 12
        private const val STANDARD_MARGIN_START = 20
        private const val STANDARD_COLOR = 0
    }

    private var mBulletPath: Path? = null

    override fun getLeadingMargin(first: Boolean): Int {
        return 2 * bulletRadius + marginStart + gapWidth
    }

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean,
        layout: Layout?
    ) {
         if ((text as Spanned).getSpanStart(this) == start) {
            val style = paint.style
            paint.style = Paint.Style.FILL

            val yPosition = if (layout != null) {
                val line = layout.getLineForOffset(start)
                layout.getLineBaseline(line).toFloat() - bulletRadius * 2f
            } else {
                (top + bottom) / 2f
            }

            val xPosition = (x + marginStart + dir * bulletRadius).toFloat()

            if (canvas.isHardwareAccelerated) {
                if (mBulletPath == null) {
                    mBulletPath = Path()
                    mBulletPath?.addCircle(0.0f, 0.0f, bulletRadius.toFloat(), Path.Direction.CW)
                }

                canvas.save()
                canvas.translate(xPosition, yPosition)
                canvas.drawPath(mBulletPath!!, paint)
                canvas.restore()
            } else {
                canvas.drawCircle(xPosition, yPosition, bulletRadius.toFloat(), paint)
            }

            paint.style = style
        }
    }
}
