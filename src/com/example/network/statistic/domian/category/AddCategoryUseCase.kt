package com.example.network.statistic.domian.category

import com.example.network.statistic.db.Db
import com.example.network.statistic.domian.UseCase
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class AddCategoryUseCase(val newApp: String, val findedCategory: String): UseCase<Unit>() {

    override fun execute() {
        val existedApp = transaction {
            Db.AppCategory
                .select { Db.AppCategory.app eq newApp }
                .singleOrNull()
        }?.getOrNull(Db.AppCategory.app)
        // add data to db
        if (existedApp == null) {
            transaction {
                Db.AppCategory.insert {
                    it[app] = newApp
                    it[category] = findedCategory
                }
            }
        } else {
            transaction {
                Db.AppCategory.update({ Db.AppCategory.app eq newApp }) {
                    it[category] = findedCategory
                }
            }
        }
    }
}