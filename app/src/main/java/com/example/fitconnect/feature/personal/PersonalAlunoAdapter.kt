package com.example.fitconnect.feature.personal

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PersonalAlunoAdapter(
    private var lista: List<PersonalAlunoVinculo>,
    private val onSelecionar: (PersonalAlunoVinculo) -> Unit
) : RecyclerView.Adapter<PersonalAlunoAdapter.VH>() {

    private var selecionadoId: Int = 0

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNome: TextView = view.findViewById(R.id.tv_nome_aluno_vinculado)
        val tvEmail: TextView = view.findViewById(R.id.tv_email_aluno_vinculado)
        val tvBadge: TextView = view.findViewById(R.id.tv_badge_aluno_selecionado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aluno_vinculado, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val vinculo = lista[position]
        val aluno = vinculo.usuarios
        val nome = aluno?.nome_usuario?.takeIf { it.isNotBlank() }
            ?: aluno?.nome_completo?.takeIf { it.isNotBlank() }
            ?: "Aluno ${vinculo.aluno_id}"

        holder.tvNome.text = nome
        holder.tvEmail.text = aluno?.email.orEmpty().ifEmpty { "Email nao informado" }
        holder.tvBadge.visibility = if (vinculo.id == selecionadoId) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener {
            selecionadoId = vinculo.id
            notifyDataSetChanged()
            onSelecionar(vinculo)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun atualizar(novaLista: List<PersonalAlunoVinculo>) {
        lista = novaLista
        if (lista.none { it.id == selecionadoId }) selecionadoId = 0
        notifyDataSetChanged()
    }
}
