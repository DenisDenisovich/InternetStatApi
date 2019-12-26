package com.example.network.statistic.domian.user

import com.example.network.statistic.db.Db
import com.example.network.statistic.domian.UseCase
import com.example.network.statistic.models.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class AddUserUseCase(private val user: User) : UseCase<Boolean>() {

    /**
     * @return false, if already exist
     * */
    override fun execute(): Boolean {
        val existedUserID = transaction {
            Db.Users.select { Db.Users.userId eq user.id }.singleOrNull()
        }?.getOrNull(
            Db.Users.userId
        )
        if (existedUserID == null) {
            transaction {
                Db.Users.insert {
                    it[userId] = user.id
                    it[info] = user.info
                }
            }
        } else {
            val info = transaction {
                Db.Users.select { Db.Users.userId eq user.id }.singleOrNull()
            }?.getOrNull(
                Db.Users.info
            )
            if (info == null) {
               transaction {
                   Db.Users.update({Db.Users.userId eq user.id}) {
                       it[Db.Users.info] = user.info
                   }
               }
            }
        }
        return existedUserID == null
    }
}