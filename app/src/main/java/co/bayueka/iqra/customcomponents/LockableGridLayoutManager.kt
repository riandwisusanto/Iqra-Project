package co.bayueka.iqra.customcomponents

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager

class LockableGridLayoutManager: GridLayoutManager {
    private var scrollable = true

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context?, spanCount: Int): super(context, spanCount)
    constructor(context: Context?, spanCount: Int, orientation: Int, reverseLayout: Boolean): super(context, spanCount, orientation, reverseLayout)

    fun setScrollingEnabled(enabled: Boolean) {
        scrollable = enabled
    }

    override fun canScrollVertically(): Boolean {
        return scrollable && super.canScrollVertically()
    }
}