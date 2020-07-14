package ru.skillbranch.skillarticles.ui.articles

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.ui.custom.ArticleItemView

class ArticlesAdapter(private val listener: (ArticleItemData, Boolean) -> Unit) :
    PagedListAdapter<ArticleItemData, ArticleVH>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {

//        val containerView =
//            LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
//        return ArticleVH(containerView)

        val containerView = ArticleItemView(parent.context)
        return ArticleVH(containerView)
    }

    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        holder.bind(getItem(position), listener)
    }
}

class ArticleDiffCallback : DiffUtil.ItemCallback<ArticleItemData>() {

    override fun areItemsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ArticleItemData, newItem: ArticleItemData): Boolean =
        oldItem == newItem

    //убирает мерцание элементов в списке, также можно использовать stableId
    //https://medium.com/@hanru.yeh/recyclerviews-views-are-blinking-when-notifydatasetchanged-c7b76d5149a2
    override fun getChangePayload(oldItem: ArticleItemData, newItem: ArticleItemData) = Any()
}

class ArticleVH(override val containerView: View) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(
        item: ArticleItemData?,
        listener: (ArticleItemData, Boolean) -> Unit
    ) {
        //item may be null if we use placeholder in paged list
        (containerView as ArticleItemView).bind(item!!, listener)
    }

}