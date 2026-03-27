//
//  GroupDialogueMode.swift
//  iosApp
//
//  Created by gergana on 3/27/26.
//

enum GroupDialogMode: Identifiable {
    case choice
    case join
    case create

    var id: Self { self }
}
