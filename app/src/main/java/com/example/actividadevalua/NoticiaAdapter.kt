package com.example.actividadevalua

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class NoticiaAdapter(
    private val context: Context,
    private val noticias: List<NoticiaModel>
) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = noticias.size
    override fun getItem(pos: Int): Any = noticias[pos]
    override fun getItemId(pos: Int): Long = pos.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = inflater.inflate(R.layout.item_noticia, parent, false)
            holder = ViewHolder(
                tvCategoria = view.findViewById(R.id.tvCategoria),
                tvTitulo    = view.findViewById(R.id.tvTitulo),
                ivPortada   = view.findViewById(R.id.ivPortada),
                tvFecha     = view.findViewById(R.id.tvFecha),
                tvUrl       = view.findViewById(R.id.tvUrl)
            )
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val noticia = noticias[position]

        holder.tvCategoria.text = noticia.ntCategoria
        try {
            holder.tvCategoria.setTextColor(Color.parseColor(noticia.ntCategoriaColor))
        } catch (e: IllegalArgumentException) {
            holder.tvCategoria.setTextColor(Color.parseColor("#2196F3"))
        }

        holder.tvTitulo.text = noticia.ntTitulo

        val imageUrl = if (noticia.ntUrlPortada.startsWith("http")) {
            noticia.ntUrlPortada
        } else {
            ApiConstants.IMAGE_BASE_URL + noticia.ntUrlPortada
        }
        Log.d("PORTADA_URL", "Cargando imagen: $imageUrl")

        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.noticia)
            .error(R.drawable.noticia)
            .centerCrop()
            .into(holder.ivPortada)

        val fecha = if (noticia.ntFechaPublicacion.contains("|"))
            noticia.ntFechaPublicacion.split("|")[0]
        else noticia.ntFechaPublicacion
        holder.tvFecha.text = context.getString(R.string.published_on) + fecha

        val fullUrl = ApiConstants.NEWS_BASE_URL + noticia.ntUrlNoticia
        holder.tvUrl.text = fullUrl
        holder.tvUrl.paintFlags = holder.tvUrl.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        holder.tvUrl.setOnClickListener {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
        }

        return view
    }

    private data class ViewHolder(
        val tvCategoria: TextView,
        val tvTitulo: TextView,
        val ivPortada: ImageView,
        val tvFecha: TextView,
        val tvUrl: TextView
    )
}
