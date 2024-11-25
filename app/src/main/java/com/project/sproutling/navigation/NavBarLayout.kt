package com.project.sproutling.navigation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    items: List<NavigationItem>,
    currentRoute: String?,
    onClick: (NavigationItem) -> Unit,
) {
    NavigationBar(
        containerColor= Color(146, 185, 145)
    ) {
        items.forEachIndexed { index, navigationItem ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(100, 145, 115),
                    selectedTextColor = Color(105, 137, 116),
                    indicatorColor = Color(186, 209, 194),
                    unselectedIconColor = Color.White,
                    unselectedTextColor = Color.White
                ),
                selected = currentRoute == navigationItem.route,
                onClick = {
                    onClick(navigationItem)
                          },
                icon = {
                    BadgedBox(badge = {
                        if (navigationItem.badgeCount != null) {
                            Badge {
                                Text(text = navigationItem.badgeCount.toString())
                            }
                        } else if (navigationItem.hasBadgeDot) {
                            Badge()
                        }
                    }) {
                        Icon(
                            imageVector = if (currentRoute == navigationItem.route) {
                                navigationItem.selectedIcon
                            } else {
                                navigationItem.unSelectedIcon
                            }, contentDescription = navigationItem.title
                        )
                    }
                },
                alwaysShowLabel = false)
        }
    }
} 