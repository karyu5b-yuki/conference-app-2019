package io.github.droidkaigi.confsched2019.data.db.entity

interface ContributorEntity {
    val id: Int
    val name: String
    val iconUrl: String
    val profileUrl: String
    val type: String
    val order: Int
}
