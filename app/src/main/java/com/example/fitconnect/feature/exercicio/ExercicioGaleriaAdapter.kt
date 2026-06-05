package com.example.fitconnect.feature.exercicio

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ExercicioGaleriaAdapter(
    private var lista: List<GaleriaExercicioBanco>,
    private val onItemClick: (GaleriaExercicioBanco) -> Unit = {}
) : RecyclerView.Adapter<ExercicioGaleriaAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNome: TextView = view.findViewById(R.id.tv_nome_exercicio)
        val tvCategoria: TextView = view.findViewById(R.id.tv_categoria_exercicio)
        val tvDificuldade: TextView = view.findViewById(R.id.tv_dificuldade)
        val tvEquipamento: TextView = view.findViewById(R.id.tv_equipamento)
        val ivGif: ImageView = view.findViewById(R.id.iv_gif_exercicio)
        val ivIconePadrao: ImageView = view.findViewById(R.id.iv_icone_padrao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercicio_galeria, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ex = lista[position]
        holder.tvNome.text = ex.nome
        holder.tvCategoria.text = ex.categoria
        holder.tvDificuldade.text = ex.dificuldade
        holder.tvEquipamento.text = ex.equipamento

        holder.itemView.setOnClickListener { onItemClick(ex) }

        if (ex.gif_url.isNotEmpty()) {
            holder.ivIconePadrao.visibility = View.GONE
            Glide.with(holder.ivGif.context)
                .load(ex.gif_url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_fitness_topic)
                .error(R.drawable.ic_fitness_topic)
                .into(holder.ivGif)
        } else {
            holder.ivIconePadrao.visibility = View.VISIBLE
            holder.ivGif.setImageDrawable(null)
        }
    }

    override fun getItemCount() = lista.size

    fun atualizar(nova: List<GaleriaExercicioBanco>) {
        lista = nova
        notifyDataSetChanged()
    }
}