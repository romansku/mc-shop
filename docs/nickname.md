# Nickname Shop

### Реализация продажи префиксов, суффиксов, их цвета и цвета ника.
Продажа используется

### Полный ник пользователя

Кониг для плагинов:
* SimpleChat
```yaml
format: "\
  %luckperms_meta_rang_prefix%\
  <reset>%luckperms_meta_custom_prefix_color%%luckperms_meta_custom_prefix_text%\
  <reset>%luckperms_meta_custom_nick_color%%name%\
  <reset>%luckperms_meta_custom_suffix_color%%luckperms_meta_custom_suffix_text% \
  <reset><gray>»</gray> %luckperms_meta_custom_chat_color%%message%"
```

%luckperms_meta_rang_prefix% <reset>%luckperms_meta_custom_prefix_color%%luckperms_meta_custom_prefix_text%
* TAB
```yaml
# put string in DB
default: # Название вашей группы в LuckPerms
  tabprefix: "\
    %luckperms_meta_rang_prefix%\
    <reset>%luckperms_meta_custom_prefix_color%%luckperms_meta_custom_prefix_text%"
  tagprefix: "\
    %luckperms_meta_rang_prefix%\
    <reset>%luckperms_meta_custom_prefix_color%%luckperms_meta_custom_prefix_text%"
  customtabname: "<reset>%luckperms_meta_custom_nick_color%%player%"
  tabsuffix: "<reset>%luckperms_meta_custom_suffix_color%%luckperms_meta_custom_suffix_text%"
  tagsuffix: "<reset>%luckperms_meta_custom_suffix_color%%luckperms_meta_custom_suffix_text%"
```

Подробное описание:
```yaml
prefix:
  rang:
    placeholder: %luckperms_meta_rang_prefix%
    description:
      преффикс группы,
      для группы default не установлен,
      для группы vip `<green>⁑`
  custom_prefix_color:
    placeholder: %luckperms_meta_custom_prefix_color%
    description:
      кастомный цвет преффикса,
      для группы default `<yellow>`
      для группы vip `<green>`
      можно купить в магазине
  custom_prefix_text:
    placeholder: %luckperms_meta_custom_prefix_text%
    description:
      кастомный текст префикса, по умолчанию не установлен.
      можно купить в магазине`
username:
    custom_nick_color:
      placeholder: %luckperms_meta_custom_nick_color%
      description:
        кастомный цвет ника,
        для группы default `<yellow>`
        для группы vip `<dark_green>`
        можно купить в магазине
    nickname:
      placeholder:
        tab: %player%
        simplechat: %name%
      description: не кастомизируется
suffix:
  custom_suffix_color:
    placeholder: %luckperms_meta_custom_suffix_color%
    description:
      кастомный цвет суффикса,
      для группы default `<yellow>`
      для группы vip `<green>`
      можно купить в магазине
  custom_suffix_text:
    placeholder: %luckperms_meta_custom_suffix_text%
    description:
      кастомный текст суффикса, по умолчанию не установлен.
      можно купить в магазине`
chat:
  custom_chat_color:
    placeholder: %luckperms_meta_custom_chat_color%
    description:
      кастомный цвет чата,
      для группы default `<light_gray>`
      для группы vip `<yellow>`
      можно купить в магазине
```

### Permission tree
```yaml
mshop:
  nickname:
    prefix:
      text: позволяет выбрать текст префикса (5 символов)
      color: позволяет выбрать цвет префикса
    name:
      color: позволяет выбрать цвет ника
    suffix:
      text: позволяет выбрать текст суффикса (5 символов)
      color: позволяет выбрать цвет суффикса
    chat:
      color: позволяет выбрать цвет чата
```