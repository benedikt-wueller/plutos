<template>
  <div class="block p-6 bg-white border border-gray-200 rounded-lg shadow-md">
    <div v-if="name" class="font-semibold text-xl flex">
      <div class="flex-grow">{{ name }}</div>
      <div v-if="icon"><font-awesome-icon :icon="icon"></font-awesome-icon></div>
    </div>
    <slot></slot>
  </div>
</template>

<script>
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default {
  name: "Card",
  components: {FontAwesomeIcon},
  props: ['name', 'icon']
}
</script>

<style scoped>

</style>