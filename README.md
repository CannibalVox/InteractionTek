# InteractionTek

An interactions mod for modders who love interactions.

This mod adds a small-but-growing number of new interactions and extensible data 
  types to Hytale. Each based around a theme or new feature.

## Item Target

Interaction chains have the concept of a "held item" - this isn't necessarily a 
  held item. In `Equipped` interactions, the "held item" is the item that was just
  equipped, for instance. However, the held item isn't intended to be changed
  and in many cases has a very specific meaning (for most Hypixel interactions
  that mess with items, there is an assumption that the held item will actually be
  the item in the User entity's active hotbar slot).

InteractionTek adds the concept of an "item target", to go with the block and
  entity target. At the beginning of a new
  interaction chain, the item target is always the held item. The target can
  be changed, though, and all of InteractionTek's interactions that mess with
  items will target the item target.


### TekItemCondition

This interaction allow you to specify a list of item matchers to compare against
  the current item target. The interaction will fail if the matchers fail. 
  InteractionTek has a large number of matcher types, with full in-asset-editor
  documentation.  You can also create your own!

**TekItemCondition Fields**

| Field Name | Type | Required? | Notes |
|------------|------|-----------|-------|
| Matchers | `Array`<br />(Element Type: `ItemMatcher`) | **Yes** | A list of matchers that will examine the target item. |
| ItemMatchType | `ItemMatchType`<br />(Default: All) | **No** | Whether all of the matchers need to pass for this interaction to pass, or any. |

**ItemMatchType Values**
- `All`
- `Any`

**Available ItemMatcher Types**

For more information, examine the types in the asset editor.

- `Armor` - Matches armor pieces
- `AssetTag` - Matches items with at least one provided asset tag
- `Category` - Matches items with at least one provided category
- `DropOnDeath` - Matches items that drop on death
- `Durability` - Matches items whose durability is more than or less than a value
- `EmptySlot` - Matches empty slots
- `Glider` - Matches gliders
- `Group` - Allows matchers to be grouped with a different ItemMatchType for complex logical operations
- `Inventory` - Matches items that belong to one of the specified inventory sections
- `ItemState` - Matches items that belong to one of the specified item states
- `ItemType` - Matches items that belong to one of the specified item types
- `PortalKey` - Matches portal keys. May optionally require a specified type
- `Quantity` - Matches items whose quantity is more or less than a value
- `Resource` - Matches items with at least one of the provided resources
- `Slot` - Compares the target item's slot against a `Slot` matcher
- `Tool` - Matches tools. May optionally require a provided spec
- `Weapon` - Matches weapons

**Available Slot Types**

You can make your own of these, too!

- `ActiveHotbar` - Matches the currently-active hotbar slot
- `ActiveUtility` - Matches the currently-active utility slot
- `IndexedSlot` - Matches slots with the provided slot index
- `InteractionHeldItem` - Matches the interaction chain's current held item
- `TargetArmorSlot` - If the target item is an armor piece, matches the armor's equipped slot
- `TargetSlot` - Will match the interaction chain's current target slot.
- `AnyOtherSlot` - Will match every slot except the interaction chain's current target item's slot


### TekTargetFirstItem

This interaction will scan the User entity's inventory slots and compare each slot
  against a set of ItemMatchers.  The first slot to succeed will be made the item
  target for the rest of the interaction chain. If no slots succeed, this interaction
  will fail.

**TekTargetFirstItem Fields**

| Field Name | Type | Required? | Notes                                                                                                                                                                                              |
|------------|------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Matchers | `Array`<br />(Element Type: `ItemMatcher`) | **Yes** | A list of matchers that will examine each item.                                                                                                                                                    |
| ItemMatchType | `ItemMatchType`<br />(Default: All) | **No** | Whether all of the matchers need to pass for a slot to be targeted, or any.                                                                                                                        |
| InventorySections | `Array`<br />(Element Type: `Integer`) | **No** | A list of inventory section ids. If provided, this field defines which sections will be scanned and in which order. By default, all sections are scanned, with the hotbar and utility slots first. |


### TekModifyItem

This interaction applies a series of modifications to the current item target. 
  The interaction will fail if any modifications fail.  Once again, there are
  several modification types and you can create your own.

**TekModifyItem Fields**

| Field Name | Type | Required? | Notes                                                                       |
|------------|------|-----------|-----------------------------------------------------------------------------|
| ItemModifications | `Array`<br />(Element Type: `ItemModification`) | **Yes** | A list of modifications that will be applied to the target item one at a time. |
| ContinueOnFailure | `Boolean`<br />(Default: false) | **No** | If true, this interaction will not stop executing modifications when one fails. It will attempt all modifications once before failing. |
| RequiredGameMode | `GameMode` | **No** | If the User entity is a player and this field is provided, the player will need to be in the specified game mode for the modificatoins to execute. Otherwise, the interaction will do nothing and be marked successful. |
| RollbackOnFailure | `Boolean`<br />(Default: false) | **No** | If true, if any modification fails, all changes made by this interaction will be reversed before it is marked as failed. |

**GameMode Values**

- `Adventure`
- `Creative`

**Available ItemModification Types**

For more information, examine the types in the asset editor.

- `AdjustDurability` - Make changes to the target item's durability, handle item breakage.
- `AdjustQuantity` - Make changes to the target item's quantity
- `ChangeItem` - Transition the target item to different item types and/or item states. Existing item states can be matched with wildcards
- `Conditional` - Execute a modification only if a set of ItemMatchers succeed
- `Group` - Execute a set of modifications. Great to use with Conditional or Singulate.
- `RelocateItemModification` - Move target item to an available slot and change the item target to its new location. Can be used for equipping and unequipping items and so much more.
- `Singulate` - If the target item has a quantity greater than 1, execute a modification on only one of them and place the rest back in the User entity's inventory


## Flow Control

Assorted flow control interactions can be used to compose larger innovations.

### TekInterruptSelf

This interaction cancels the current interaction chain and any active forked 
  interaction chains like Hypixel's `Interrupt` interaction does for interactions
  running on targeted entities.  Afterward, the interaction chain will escape to
  the root without running further interactions. 

### TekRandomBranch

This interaction will randomly select what interaction to run next from a list
  of possible branches.  The selection will use a weighted random selection,
  allowing some options to be more likely than others.

**TekRandomBranch Fields**

| Field Name | Type | Required? | Notes                                                                       |
|------------|------|-----------|-----------------------------------------------------------------------------|
| Branches | `Array`<br />(Element Type: `Branch`) | **Yes** | The list of options and their random weights. |

**Branch Fields**

| Field Name | Type | Required? | Notes                                                                         |
|------------|------|-----------|-------------------------------------------------------------------------------|
| Interaction | `Asset`<br />(Asset Type: `Interaction`) | **Yes** | The interaction to run if this branch is selected.                            |
| Weight | `Integer`<br />(Default: 1) | **No** | The weight assigned to this branch.  Higher means more likely to be selected. |


## Utility

Connections to other modding systems.

### TekRunCommand

This interaction executes a command as one of the interaction's entities, if possible.  By
  default, players are the only entity type that can send commands, but if you have a mod that
  adds other capabilities, this interaction will be compatible.

This interaction will fail if the specified entity cannot run commands but will otherwise succeed,
  even if the command fails or if the syntax is invalid.

| Field Name | Type | Required? | Notes                                                                         |
|------------|------|-----------|-------------------------------------------------------------------------------|
| RunAs | `InteractionTarget`<br />(Default: User) | **No** | The entity to run the command as. |
| CommandText | `String` | **Yes** | The command text to run. Exclude the leading slash. |

**InteractionTarget Values**
- `User`
- `Owner`
- `Target`

### TekRunProxiedCommand

This interaction will execute a command as one of the interaction's entities if possible, but proxied through
  a non-player structure with enhanced permissions.  As a result, commands that require a player in order to function will
  require use of the `--player` flag. However, this command also allows use of @-variables, a bit like minecraft command
  blocks.  Any @ symbol that isn't immediately followed by a valid variable name will be displayed normally.  Double
  @ symbols will always be displayed as a single @ symbol and not perform variable processing.

Examples:

`CommandText`: say Hello, @player!
`Output`: Hello, Canvoxtek!

`CommandText`: say @_@
`Output`: @_@

`CommandText`: say No, @secrets is not a variable.
`Output`: No, @secrets is not a variable.

`CommandText`: say To send the player's display name, use @@player!
`Output`: To send the player's display name, use @player!

`CommandText`: say @@@@ @@_@@ @@@@
`Output`: @@ @_@ @@

The following @-variables are currently implemented:

| Variable | Notes                                                                                                                                     |
|----------|-------------------------------------------------------------------------------------------------------------------------------------------|
| @player  | The player display name of the specified entity. If the specified entity is not a player, this interaction will fail.                     |
| @self    | The UUID of the User entity. If the specified entity does not have a UUID, this interaction will fail.                                    |
| @selfX   | The block X position of the User entity. If the specified entity does not have a position, this interaction will fail.                    |
| @selfY   | The block Y position of the User entity. If the specified entity does not have a position, this interaction will fail.                    |
| @selfZ   | The block Z position of the User entity. If the specified entity does not have a position, this interaction will fail.                    |
| @blockX  | The X position of the target block. If there is no target block, this interaction will fail.                                              |
| @blockY  | The Y position of the target block. If there is no target block, this interaction will fail.                                              |
| @blockZ  | The Z position of the target block. If there is no target block, this interaction will fail.                                              |
| @targetX | The block X position of the Target entity. If there is no target entity or the target entity has no position, this interaction will fail. |
| @targetY | The block Y position of the Target entity. If there is no target entity or the target entity has no position, this interaction will fail. |
| @targetZ | The block Z position of the Target entity. If there is no target entity or the target entity has no position, this interaction will fail. |

This interaction will fail if the specified entity cannot run commands with the provided permissions or if an invalid variable is
  used, but will otherwise succeed, even if the command fails or if the syntax is invalid.

| Field Name | Type | Required? | Notes                                                                         |
|------------|------|-----------|-------------------------------------------------------------------------------|
| RunAs | `InteractionTarget`<br />(Default: User) | **No** | The entity to run the command as. |
| CommandText | `String` | **Yes** | The command text to run. Exclude the leading slash. |
| WithPermissions | `Set`<br />(Element Type: `String`) | **No** | **If this field is not provided, the command will be run with complete, unlimited permissions.**  If provided, this set of permissions will be added to the specified entity's permissions within the scope of this command. |

**InteractionTarget Values**
- `User`
- `Owner`
- `Target`
