package com.example.fitconnect.feature.exercicio

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

// 1. Modelo de Dados para o Exercício
data class Exercicio(
    val nome: String,
    val seriesReps: String,
    val gifUrl: String = "",
    val categoria: String = "",
    val dificuldade: String = "",
    val equipamento: String = "",
    val instrucoes: String = ""
)

// 2. Adapter
class ExercicioDetalheAdapter(private val listaExercicios: List<Exercicio>) : RecyclerView.Adapter<ExercicioDetalheAdapter.ExercicioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExercicioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercicio_detalhe, parent, false)
        return ExercicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExercicioViewHolder, position: Int) {
        val exercicio = listaExercicios[position]
        holder.tvNome.text = exercicio.nome
        holder.tvSeriesReps.text = exercicio.seriesReps
        if (exercicio.gifUrl.isNotBlank()) {
            Glide.with(holder.ivExercicio.context)
                .load(exercicio.gifUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_fitness_topic)
                .error(R.drawable.ic_fitness_topic)
                .into(holder.ivExercicio)
        } else {
            holder.ivExercicio.setImageResource(R.drawable.ic_fitness_topic)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetalheExercicioTreinoActivity::class.java).apply {
                putExtra("EXERCICIO_NOME", exercicio.nome)
                putExtra("EXERCICIO_SERIES_REPS", exercicio.seriesReps)
                putExtra("EXERCICIO_CATEGORIA", exercicio.categoria)
                putExtra("EXERCICIO_DIFICULDADE", exercicio.dificuldade)
                putExtra("EXERCICIO_EQUIPAMENTO", exercicio.equipamento)
                putExtra("EXERCICIO_GIF_URL", exercicio.gifUrl)
                putExtra("EXERCICIO_INSTRUCOES", exercicio.instrucoes)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listaExercicios.size
    }

    class ExercicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNome: TextView = itemView.findViewById(R.id.tv_nome_exercicio_detalhe)
        val tvSeriesReps: TextView = itemView.findViewById(R.id.tv_reps_exercicio_detalhe)
        val ivExercicio: ImageView = itemView.findViewById(R.id.iv_exercicio_detalhe)
    }
}
