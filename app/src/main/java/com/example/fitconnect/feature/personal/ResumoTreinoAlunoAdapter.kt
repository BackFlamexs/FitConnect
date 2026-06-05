package com.example.fitconnect.feature.personal

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResumoTreinoAlunoAdapter(
    private var lista: List<TreinoBanco>
) : RecyclerView.Adapter<ResumoTreinoAlunoAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTagDia: TextView = view.findViewById(R.id.tv_resumo_tag_dia)
        val tvNome: TextView = view.findViewById(R.id.tv_resumo_nome_treino)
        val tvDiaSemana: TextView = view.findViewById(R.id.tv_resumo_dia_semana)
        val tvDetalhes: TextView = view.findViewById(R.id.tv_resumo_detalhes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resumo_treino_aluno, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val treino = lista[position]
        holder.tvTagDia.text = treino.tag_dia.ifBlank { "--" }
        holder.tvNome.text = treino.nome.ifBlank { "Treino sem nome" }
        holder.tvDiaSemana.text = treino.dia_semana.ifBlank { "Dia nao definido" }
        holder.tvDetalhes.text = treino.detalhes.ifBlank { "Sem categoria" }
    }

    override fun getItemCount(): Int = lista.size

    fun atualizar(novaLista: List<TreinoBanco>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}
