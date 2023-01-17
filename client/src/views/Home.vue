<template>
  <div>
    <breadcrumbs></breadcrumbs>

    <h1 class="text-2xl font-semibold mb-4">Home</h1>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <card name="Statements" class="cursor-pointer hover:bg-gray-100" @click="this.$router.push('/statements')" icon="fa-solid fa-receipt">
        <div class="mt-2">
          View, filter and manage statements for all accounts.
        </div>
      </card>
      <card name="Accounts" class="cursor-pointer hover:bg-gray-100" @click="this.$router.push('/accounts')" icon="fa-solid fa-wallet">
        <div class="mt-2">
          Create, edit and delete accounts.
        </div>
      </card>
      <card name="Categories & Budgets" class="cursor-pointer hover:bg-gray-100" @click="this.$router.push('/categories')" icon="fa-solid fa-coins">
        <div class="mt-2">
          Create, edit and delete categories and budgets.
        </div>
      </card>
      <card name="Tags" class="cursor-pointer hover:bg-gray-100" @click="this.$router.push('/tags')" icon="fa-solid fa-tag">
        <div class="mt-2">
          Create, edit and delete tags.
        </div>
      </card>
    </div>
  </div>
</template>

<script>
import Breadcrumbs from "../components/Breadcrumbs.vue";
import Card from "../components/Card.vue";

export default {
  name: "Home",
  components: {Card, Breadcrumbs}
}
</script>

<style scoped>

</style>