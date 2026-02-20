# InteractionTek

An interactions mod for modders who love interactions.

This mod adds a small-but-growing number of new interactions and extensible data 
  types to Hytale. Each based around a theme or new feature.

## Item Target

Interaction chains have the concept of a "held item" - this isn't necessarily a 
  held item. In `Equipped` interaction, the "held item" is the item that was just
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
- `Interaction` - Matches items with at least one interaction of the provided types
- `Inventory` - Matches items that belong to one of the specified inventory sections
- `ItemState` - Matches items that belong to one of the specified item states
- `ItemType` - Matches items that belong to one of hte specified item types
- `PortalKey` - Matches portal keys. May optionally require a specified type
- `Quantity` - Matches items whose quantity is more or less than a value
- `Resource` - Matches items with at least one of the provided resources
- `Slot` - Compares the target item's slot against a `Slot` matcher
- `Tool` - Matches tools. May optionally require a provided spec
- `Usable` - Matches usable items
- `Weapon` - Matches weapons

**Available Slot Types**

You can make your own of these, too!

- `ActiveHotbar` - Matches the currently-active hotbar slot
- `ActiveUtility` - Matches the currently-active utility slot
- `IndexedSlot` - Matches slots with the provided slot index
- `InteractionHeldItem` - Matches the interaction chain's current held item


### TekTargetFirstItem

This interaction will scan the User entity's inventory slots and compare each slot
  against a set of ItemMatchers.  The first slot to succeed will be made the item
  target for the rest of the interaction chain.

**TekTargetFirstItem Fields**

| Field Name | Type | Required? | Notes                                                                       |
|------------|------|-----------|-----------------------------------------------------------------------------|
| Matchers | `Array`<br />(Element Type: `ItemMatcher`) | **Yes** | A list of matchers that will examine the target item.                       |
| ItemMatchType | `ItemMatchType`<br />(Default: All) | **No** | Whether all of the matchers need to pass for a slot to be targeted, or any. |
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

**GameMode Values**

- `Adventure`
- `Creative`

**Available ItemModification Types**

For more information, examine the types in the asset editor.

- `AdjustDurability` - Make changes to the target item's durability, handle item breakage.
- `AdjustQuantity` - Make changes to the target item's quantity
- `ChangeState` - Transition the target item between item states
- `Conditional` - Execute a set of modifications only if a set of ItemMatchers succeed
- `Singulate` - If the target item has a quantity greater than 1, execute a modification on only one of them and place the rest back in the User entity's inventory

