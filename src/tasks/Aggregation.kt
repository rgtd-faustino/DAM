package tasks

import contributors.User

/*
TODO: Write aggregation code.

 In the initial list each user is present several times, once for each
 repository he or she contributed to.
 Merge duplications: each user should be present only once in the resulting list
 with the total value of contributions for all the repositories.
 Users should be sorted in a descending order by their contributions.

 The corresponding test can be found in test/tasks/AggregationKtTest.kt.
 You can use 'Navigate | Test' menu action (note the shortcut) to navigate to the test.
*/
/*
group
"alice" -> [User("alice", 10), User("alice", 5)]
"bob"   -> [User("bob", 3)]
map
User("alice", 15)  // 10 + 5
User("bob", 3)
sort
User("alice", 15)  // primeiro
User("bob", 3)     // segundo
*/
fun List<User>.aggregate(): List<User> =
    // agrupa os utilizadores pelos nomes em grupos
    groupBy { it.login }
        // e para cada novo grupo com um determinado nome vai criar um user com a soma de todas as contribuições
        .map { (login, group) -> User(login, group.sumOf { it.contributions }) }
        .sortedByDescending { it.contributions } // decrescente pelo número de contribuiições