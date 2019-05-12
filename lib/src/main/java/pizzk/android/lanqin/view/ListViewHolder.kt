package pizzk.android.lanqin.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

}

abstract class ListViewAdapter<T>(val context: Context) : RecyclerView.Adapter<ListViewHolder>() {
    private val list: MutableList<T> = ArrayList(0)

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    abstract fun getLayoutId(viewType: Int): Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val layoutId: Int = getLayoutId(viewType)
        val view: View = LayoutInflater.from(context).inflate(layoutId, null)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int = list.size
}