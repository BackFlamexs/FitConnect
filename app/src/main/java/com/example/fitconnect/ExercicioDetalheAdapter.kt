package com.example.fitconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 1. Modelo de Dados para o Exercício
data class Exercicio(
    val nome: String,
    val seriesReps: String
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
    }

    override fun getItemCount(): Int {
        return listaExercicios.size
    }

    class ExercicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNome: TextView = itemView.findViewById(R.id.tv_nome_exercicio_detalhe)
        val tvSeriesReps: TextView = itemView.findViewById(R.id.tv_reps_exercicio_detalhe)
    }
}
