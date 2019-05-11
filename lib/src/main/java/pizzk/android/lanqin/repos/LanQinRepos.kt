package pizzk.android.lanqin.repos

import pizzk.android.lanqin.entity.LanQinEntity

abstract class LanQinRepos {

    abstract fun save(entities: List<LanQinEntity>): Int

}