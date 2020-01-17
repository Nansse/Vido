package com.vido.ui.dashboard.DranNDrop

import android.Manifest
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.vido.R
import com.vido.ui.dashboard.DashboardFragment
import com.vido.ui.dashboard.Plan
import com.vido.ui.dashboard.Plans

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    ItemTouchHelperViewHolder {
    val row_plans: ConstraintLayout

    init {
        row_plans = itemView as ConstraintLayout
    }

    override fun onItemClear() {
        //row_plans.setBackgroundColor(Color.LTGRAY);
    }

    override fun onItemSelected() {
        //row_plans.setBackgroundColor(0);
    }
}

class RecyclerViewAdapter(context: Context, plans: Plans, fragment: DashboardFragment): RecyclerView.Adapter<ItemViewHolder>(),
    ItemTouchHelperAdapter {

    private val mPlans: Plans
    private val mContext: Context
    private val mFragment: DashboardFragment

    init {
        mContext = context
        mPlans = plans
        mFragment = fragment
    }

    override fun getItemCount(): Int {
        return mPlans.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder{
        val layoutInflater = LayoutInflater.from(parent.context)
        val row_plans = layoutInflater.inflate(R.layout.row_plan, parent, false)
        return ItemViewHolder(row_plans)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.row_plans.findViewById<TextView>(R.id.plan_number_view).text = "Plan ${mPlans.at(position).originalIndex + 1}"
        holder.row_plans.findViewById<TextView>(R.id.timeView).text = "${mPlans.at(position).duration}s"
        holder.row_plans.findViewById<ImageButton>(R.id.delete_button).tag = mPlans.at(position).uniqueID
        holder.row_plans.findViewById<ImageButton>(R.id.delete_button).setOnClickListener { view -> deleteVideo(view.tag.toString()) }
        holder.row_plans.findViewById<ImageButton>(R.id.play_button).tag = mPlans.at(position).uniqueID
        holder.row_plans.findViewById<ImageButton>(R.id.play_button).setOnClickListener { view -> playVideo(view.tag.toString()) }
    }

    private fun playVideo(uniqueId: String) {
        mFragment.playVideo(Plans.instance.findWithUniqueId(uniqueId).index)
    }
    private fun deleteVideo(uniqueId: String) = mContext.runWithPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
        mPlans.removeAt(Plans.instance.findWithUniqueId(uniqueId).index)
        mFragment.updateThumbnail()
        notifyDataSetChanged()
    }

    override fun onItemDismiss(position: Int) {
        // TODO
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        mPlans.move(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition);
    }
}