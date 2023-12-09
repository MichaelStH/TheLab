package com.riders.thelab.ui.kat

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.riders.thelab.core.common.utils.DateTimeUtils
import com.riders.thelab.core.data.remote.dto.kat.Kat
import com.riders.thelab.databinding.RowKatMessageOtherBinding
import com.riders.thelab.databinding.RowKatMessageSelfBinding
import timber.log.Timber

class KatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var rowOtherBinding: RowKatMessageOtherBinding? = null
    private var rowSelfBinding: RowKatMessageSelfBinding? = null

    constructor(selfItemView: RowKatMessageSelfBinding) : this(selfItemView.root) {
        rowSelfBinding = selfItemView
    }

    constructor(otherItemView: RowKatMessageOtherBinding) : this(otherItemView.root) {
        rowOtherBinding = otherItemView
    }

    fun bindOther(data: Kat) {
        Timber.d("bindOther() object : %s", data.toString())
        rowOtherBinding?.tvMessageSender?.text = data.senderId
        rowOtherBinding?.tvMessageContent?.text = data.message
        rowOtherBinding?.tvMessageTimestamp?.text =
            DateTimeUtils.formatMillisToTimeHoursMinutes(data.timestamp)
    }

    fun bindSelf(data: Kat) {
        Timber.d("bindSelf() object : %s", data.toString())
        rowSelfBinding?.tvMessageSender?.text = data.senderId
        rowSelfBinding?.tvMessageContent?.text = data.message
        rowSelfBinding?.tvMessageTimestamp?.text =
            DateTimeUtils.formatMillisToTimeHoursMinutes(data.timestamp)
    }
}