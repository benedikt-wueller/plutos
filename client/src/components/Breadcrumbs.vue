<template>
  <nav class="flex mb-4" aria-label="Breadcrumb">
    <ol class="inline-flex items-center space-x-2">
      <li>
        <router-link to="/" class="flex items-center">
          <font-awesome-icon icon="fa-solid fa-home" class="text-xs"></font-awesome-icon>
          <span class="ml-2 text-sm font-medium text-gray-500 hover:text-gray-800">Home</span>
        </router-link>
      </li>
      <li v-for="(breadcrumb, index) in breadcrumbs" v-bind:key="'breadcrumb-' + index">
        <router-link :to="breadcrumb.path" class="flex items-center">
          <font-awesome-icon icon="fa-solid fa-chevron-right" class="text-xs"></font-awesome-icon>
          <span class="ml-2 text-sm font-medium" :class="{
            'text-gray-800': index == breadcrumbs.length - 1,
            'text-gray-500': index != breadcrumbs.length - 1
          }">{{ breadcrumb.name }}</span>
        </router-link>
      </li>
    </ol>
  </nav>
</template>

<script>
export default {
  name: "Breadcrumbs",
  props: {
    breadcrumbs: Array
  }
}
</script>

<style scoped>

</style>