@file:Suppress("unused")
/*
 * Copyright 2025 Steven Smith kana-tutor.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kana_tutor.utils

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.kana_tutor.kvgviewer.uiThemeIsDark

private const val TAG = "MenuEnable"

// Recursively search the Menu passed in looking for the desired
// item.  If the menu has a sub-menu, search into that.
fun Menu.getMenuItem(itemId: Int): MenuItem? {
    fun getMenuItem(idx: Int, itemId: Int): MenuItem? {
        if (idx >= size()) return null
        val item = getItem(idx)
        if (item.hasSubMenu()) {
            val mi = item.subMenu!!.getMenuItem(itemId)
            // mi non-null means we found item.
            if (mi != null) {
                return mi
            }
        }
        if (item != null && item.itemId == itemId)
            return item
        return getMenuItem(idx + 1, itemId)
    }
    return getMenuItem(0, itemId)
}

fun getSubMenu(menu: Menu, title: String): Menu? {
    var rv: Menu? = null
    fun Menu.getSubMenu() {
        val itemsSize = size()
        var itemCounter = 0
        while (itemCounter < itemsSize && rv == null) {
            val item = getItem(itemCounter)
            if (item.hasSubMenu()) {
                if (item.title!!.contains(title)) {
                    rv = item.subMenu
                    break
                }
                if (rv == null)
                    item.subMenu!!.getSubMenu()
            }
            itemCounter++
        }
    }
    menu.getSubMenu()
    return rv
}
fun Context.getSubMenu(menu: Menu, resId: Int): Menu? =
    getSubMenu(menu, getString(resId))

fun Menu.menuItemEnable(itemId: Int, enableItem:Boolean) {
    val menuItem = getMenuItem(itemId)
    val spannedTitle = SpannableString(menuItem!!.title)
    menuItem.isEnabled = enableItem
    val color = if(enableItem) Color.WHITE else Color.GRAY
    spannedTitle.setSpan(
        ForegroundColorSpan(color),
        0, spannedTitle.length, 0
    )
    menuItem.title = spannedTitle
}
fun Menu.setItemColor(itemId: Int, color: Int) {
    val menuItem = getMenuItem(itemId)!!
    val title = menuItem.title!!
    val spannedTitle = SpannableString(title)
    spannedTitle.setSpan(
        ForegroundColorSpan(color),
        0, title.length, 0
    )
    menuItem.title = spannedTitle
}
fun Menu.menuItemSetTitle(itemId: Int, title: String) {
    val menuItem = getMenuItem(itemId)
    if (menuItem != null) {
        val spannedTitle = SpannableString(title)
        val color =
            if (uiThemeIsDark)
                if (menuItem.isEnabled) Color.WHITE else Color.GRAY
            else
                if (menuItem.isEnabled) Color.BLACK else Color.GRAY

        spannedTitle.setSpan(
            ForegroundColorSpan(color),
            0, title.length, 0
        )
        menuItem.title = spannedTitle
    }
    else
        Log.e(TAG,
            "menuSetText to \"$title\": Failed to find ${
                "itemId:0x%08x".format(itemId)}"
        )
}

/*
fun getForegroundColor(pos: Int): Int {
    return if (pos < 0 || pos > getText().length()) {
        DEFAULT_FOREGROUND_COLOR
    }
    else {
        val spans: Array<ForegroundColorSpan> = getText().getSpans(
            pos, pos,
            ForegroundColorSpan::class.java
        )
        if (spans.size > 0) {
            spans[0].foregroundColor
        }
        else {
            DEFAULT_FOREGROUND_COLOR
        }
    }
}

// This causes text for menu item to be grayed out when it is disabled.
class MenuEnable(menu: Menu) {
    private val itemsMap : Map<Int, MenuItem> by lazy {
        val rv = mutableMapOf<Int, MenuItem>()
        fun get(menu: Menu) {
            for (i in 0 .. menu.size() - 1) {
                val item = menu.getItem(i)
                if (item.hasSubMenu())
                    get(item.subMenu)
                else
                    rv[item.itemId] = item

            }
        }
        get(menu)
        rv
    }
    fun enableMenuItem(itemId: Int, enableItem: Boolean) {
        if (itemsMap.containsKey(itemId)) {
            val item =  itemsMap[itemId]!!
            item.isEnabled = enableItem
            val itemText = SpannableString(item.title)
            if (enableItem) {
                itemText.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    0, itemText.length, 0
                )
            }
            else {
                itemText.setSpan(
                    ForegroundColorSpan(Color.GRAY),
                    0, itemText.length, 0
                )
            }
            item.title = itemText
        }
        else
            Log.d(
                TAG,
                "enableItem: item 0x%08x not found".format(itemId)
            )class MenuUtils {
}
    }
}
 */
