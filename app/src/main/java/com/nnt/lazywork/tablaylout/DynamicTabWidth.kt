package com.nnt.lazywork.tablaylout

import android.view.ViewTreeObserver
import android.widget.LinearLayout
import com.google.android.material.tabs.TabLayout
import com.nnt.lazywork.util.getScreenWidth


/**
 * When your tab count and tab titles are dynamic and you want one line title.
 * This function will calculate the width of your tabs and determine which tab mode will be used.
 * Call the function after adding all your tabs into TabLayout
 */
fun TabLayout.dynamicSetTabLayoutMode() {
    tabMode = TabLayout.MODE_SCROLLABLE
    calculateTabWidth(this)
}
private fun setTabMode(
    tabLayout: TabLayout,
    tabTotalWidth: Int,
    tabChildMax: Int,
    tabChildrenWidth: List<Int>
) {
    val screenWidth = getScreenWidth()
    if (tabTotalWidth <= screenWidth && tabChildMax <= (screenWidth.toFloat() / tabLayout.tabCount)) {
        setupFixedMode(tabLayout)
    } else {
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        if (tabTotalWidth < screenWidth) {
            resizeTabViewToFitScreenWidth(tabLayout, tabTotalWidth, tabChildrenWidth)
        }
    }
}

private fun setupFixedMode(
    tabLayout: TabLayout
) {
    tabLayout.tabMode = TabLayout.MODE_FIXED
    val paren = (tabLayout.getChildAt(0) as LinearLayout)
    paren.weightSum = 1F
    for (i in 0 until tabLayout.tabCount) {
        val children = paren.getChildAt(i) as LinearLayout
        val param = children.layoutParams as LinearLayout.LayoutParams
        param.weight = 1F/tabLayout.tabCount
        children.layoutParams = param
    }
}

private fun resizeTabViewToFitScreenWidth(
    tabLayout: TabLayout,
    tabTotalWidth: Int,
    tabsWidth: List<Int>
) {
    val paren = (tabLayout.getChildAt(0) as LinearLayout)
    paren.weightSum = 1F
    for (i in tabsWidth.indices) {
        val children = paren.getChildAt(i) as LinearLayout
        val param = children.layoutParams as LinearLayout.LayoutParams
        param.weight = tabsWidth[i].toFloat() / tabTotalWidth.toFloat()
        children.layoutParams = param
    }
}

private fun calculateTabWidth(tabLayout: TabLayout) {
    var childWidthMax = 0
    var tabLayoutWidth = 0
    val tabChildrenWidths = ArrayList<Int>()
    for (i in 0 until tabLayout.tabCount) {
        tabLayout.getTabAt(i)?.view?.let { tabView ->
            tabView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    tabView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val childWidth = tabView.width
                    tabChildrenWidths.add(childWidth)
                    if (i == 0) {
                        childWidthMax = childWidth
                    } else {
                        if (childWidth > childWidthMax) {
                            childWidthMax = childWidth
                        }
                    }
                    tabLayoutWidth += childWidth
                    if (i == tabLayout.tabCount - 1) {
                        setTabMode(tabLayout, tabLayoutWidth, childWidthMax, tabChildrenWidths)
                    }
                }
            })
        }
    }
}