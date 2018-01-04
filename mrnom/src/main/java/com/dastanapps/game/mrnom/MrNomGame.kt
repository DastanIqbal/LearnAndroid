package com.dastanapps.game.mrnom

import com.dastanapps.gameframework.Screen
import com.dastanapps.gameframework.impl.AndroidGame

class MrNomGame : AndroidGame() {

   override fun getStartScreen(): Screen {
       return LoadingScreen(this)
   }
}
