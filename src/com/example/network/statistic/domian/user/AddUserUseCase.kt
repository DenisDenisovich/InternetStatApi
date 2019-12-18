package com.example.network.statistic.domian.user

import com.example.network.statistic.db.Db
import com.example.network.statistic.domian.UseCase
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class AddUserUseCase(private val user: String) : UseCase<Boolean>() {

    /**
     * @return false, if already exist
     * */
    override fun execute(): Boolean {
        val existedUserID = transaction {
            Db.Users.select { Db.Users.userId eq user }.singleOrNull()
        }?.getOrNull(
            Db.Users.userId
        )
        if (existedUserID == null) {
            transaction {
                Db.Users.insert {
                    it[userId] = user
                }
            }
        }
        return existedUserID == null
    }
}