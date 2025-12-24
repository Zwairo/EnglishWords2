package com.example.englishwords2

data class GameMode(val id: Int,
                    val title: String,
                    val subtitle: String,
                    val isLocked: Boolean,
                    val unlockCost: Int=0)
