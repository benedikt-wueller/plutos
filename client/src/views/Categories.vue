<template>
  <div>
    <breadcrumbs :breadcrumbs="[{ name: 'Categories', 'path': '/categories' }]"></breadcrumbs>

    <h1 class="text-2xl font-semibold mb-4">Categories & Budgets</h1>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5 gap-4 mb-4">
      <card v-for="category in categories"
            v-bind:key="'category-' + category.id"
            class="hover:bg-gray-100 cursor-pointer"
            @click="showModal(category)">
        <div class="font-semibold text-xl">
          <span>{{ category.attributes.name }}</span>
          <span class="text-gray-400" v-if="category.attributes.default"> (Default)</span>
        </div>

        <div class="grid grid-cols-3 gap-2 mt-2">
          <div>
            <p class="font-semibold">Limit</p>
            <p>{{ category.attributes.limit || 'No Limit' }} {{ category.attributes.limit ? category.attributes.currency : null }}</p>
          </div>
          <div>
            <p class="font-semibold">Patterns</p>
            <p>{{ category.relationships.patterns.data.length }}</p>
          </div>
          <div>
            <p class="font-semibold">Default</p>
            <p>{{ category.attributes.default ? 'Yes' : 'No' }}</p>
          </div>
          <div>
            <p class="font-semibold">Statements</p>
            <p>{{ category.relationships.statements.data.length }}</p>
          </div>
          <div>
            <p class="font-semibold">Tags</p>
            <p>{{ category.relationships.tags.data.length }}</p>
          </div>
        </div>
      </card>
    </div>

    <h1 class="text-2xl font-semibold mb-4">Actions</h1>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5 gap-4 mb-4">
      <card name="Add Category" icon="fa-solid fa-plus" class="bg-green-100 hover:bg-green-200 cursor-pointer" @click="showModal(null)">
        <div class="mt-2">
          Create a new category with optional budget.
        </div>
      </card>
    </div>

    <modal :title="selectedCategory.id ? 'Edit Category' : 'Add Category'" v-if="selectedCategory" @hide="hideModal">
      <div>
        <div class="font-semibold">Name</div>
        <input type="text" class="rounded-md w-full" placeholder="Name" v-model="selectedCategory.attributes.name">
      </div>

      <div class="mt-4">
        <div class="font-semibold">Budget / Limit</div>
        <input type="number" min="0" class="rounded-md w-full" placeholder="No Limit" v-model="selectedCategory.attributes.limit">
      </div>

      <div class="mt-4">
        <div class="form-check">
          <input v-model="selectedCategory.attributes.default"
                 :disabled="selectedCategory.id && $store.state.categories[selectedCategory.id].attributes.default"
                 class="form-check-input appearance-none h-4 w-4 border border-gray-300 rounded-sm bg-white checked:bg-blue-600 checked:border-blue-600 focus:outline-none transition duration-200 mt-1 align-top bg-no-repeat bg-center bg-contain float-left mr-2 cursor-pointer"
                 type="checkbox"
                 id="flexCheckDefault">
          <label class="form-check-label inline-block text-gray-800" for="flexCheckDefault">
            Make default category
          </label>
        </div>
      </div>

      <div class="mt-4 grid grid-cols-2 gap-4">
        <div>
          <div class="font-semibold">Background Color</div>
          <input type="color" class="rounded-md w-full p-1 h-10" v-model="selectedCategory.attributes.color">
        </div>
        <div>
          <div class="font-semibold">Text Color</div>
          <input type="color" class="rounded-md w-full p-1 h-10" v-model="selectedCategory.attributes.textColor">
        </div>
      </div>

      <div class="mt-4 pt-4 border-t-2 flex gap-2 flex">
        <input type="submit" value="Delete"
               v-if="selectedCategory.id && !selectedCategory.attributes.default"
               class="rounded-md bg-red-500 px-4 py-1.5 text-white cursor-pointer hover:bg-red-800"
               @click="deleteCategory">
        <div class="flex-grow"></div>
        <input type="submit" value="Edit Patterns"
               v-if="selectedCategory.id"
               class="rounded-md bg-blue-500 px-4 py-1.5 text-white cursor-pointer hover:bg-blue-800"
               @click="$router.push('/categories/' + selectedCategory.id)">
        <input type="submit" value="Save"
               class="rounded-md bg-blue-500 px-4 py-1.5 text-white cursor-pointer hover:bg-blue-800"
               @click="submitCategory">
      </div>
    </modal>
  </div>
</template>

<script>
import Breadcrumbs from "../components/Breadcrumbs.vue";
import Card from "../components/Card.vue";
import Modal from "../components/Modal.vue";

export default {
  name: "Categories",
  components: {Modal, Card, Breadcrumbs},
  data() {
    return {
      busy: false,
      selectedCategory: null,
    }
  },
  computed: {
    categories() {
      return this.$store.getters.getCategories()
    }
  },
  mounted() {
    this.$store.dispatch('fetchCategories')
  },
  methods: {
    showModal(category) {
      if (category == null) {
        category = {
          attributes: {
            name: 'New Category',
            color: '#E5E7EB',
            textColor: '#000000',
            limit: null,
            default: false,
          }
        }
      } else {
        category = JSON.parse(JSON.stringify(category))
      }
      this.selectedCategory = category
    },
    submitCategory() {
      if (this.selectedCategory == null) return;
      const action = this.selectedCategory.id == null ? 'createCategory' : 'updateCategory'
      this.$store.dispatch(action, this.selectedCategory).then(() => {
        this.selectedCategory = null
      })
    },
    deleteCategory() {
      this.$store.dispatch('deleteCategory', { id: this.selectedCategory.id }).then(() => {
        this.selectedCategory = null
      })
    },
    hideModal() {
      this.selectedCategory = null
    }
  }
}
</script>

<style scoped>

</style>