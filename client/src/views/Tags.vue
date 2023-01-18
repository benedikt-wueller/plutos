<template>
  <div>
    <breadcrumbs :breadcrumbs="[{ name: 'Tags', 'path': '/tags' }]"></breadcrumbs>

    <h1 class="text-2xl font-semibold mb-4">Tags</h1>

    <div v-if="!tags || tags.length === 0" class="mb-4 bg-blue-100 py-3 px-4 rounded-md">
      There are no tags yet.
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <card v-for="tag in tags"
            v-bind:key="'tag-' + tag.id"
            :name="tag.attributes.name"
            class="hover:bg-gray-100 cursor-pointer"
            @click="showModal(tag)">
        <div class="mt-2 grid grid-cols-2 gap-2">
          <div>
            <p class="font-semibold">Patterns</p>
            <p>{{ tag.relationships.patterns.data.length }}</p>
          </div>
          <div>
            <p class="font-semibold">Statements</p>
            <p>{{ tag.relationships.statements.data.length }}</p>
          </div>
          <div class="col-span-2">
            <p class="font-semibold">Category</p>
            <p>{{ getTagCategoryName(tag) }}</p>
          </div>
        </div>
      </card>
    </div>

    <h1 class="text-2xl font-semibold mb-4">Actions</h1>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <card name="Add Tag" icon="fa-solid fa-plus" class="bg-green-100 hover:bg-green-200 cursor-pointer" @click="showModal(null)">
        <div class="mt-2">
          Create a new tag.
        </div>
      </card>
    </div>

    <modal :title="selectedTag.id ? 'Edit Tag' : 'Add Tag'" v-if="selectedTag" @hide="hideModal">
      <div>
        <div class="font-semibold">Name</div>
        <input type="text" class="rounded-md w-full" placeholder="Name" v-model="selectedTag.attributes.name">
      </div>

      <div class="mt-4">
        <div class="font-semibold">Category</div>
        <select class="rounded-md w-full mt-2" ref="categorySelector">
          <option value="-1" :selected="!selectedTag.relationships.category.data">None</option>
          <option v-for="category in categories"
                  v-bind:key="'category-option-' + category.id"
                  :value="category.id"
                  :selected="selectedTag.relationships.category.data && selectedTag.relationships.category.data.id === category.id">
            {{ category.attributes.name }}
          </option>
        </select>
      </div>

      <div class="mt-4 grid grid-cols-2 gap-4">
        <div>
          <div class="font-semibold">Background Color</div>
          <input type="color" class="rounded-md w-full p-1 h-10" v-model="selectedTag.attributes.color">
        </div>
        <div>
          <div class="font-semibold">Text Color</div>
          <input type="color" class="rounded-md w-full p-1 h-10" v-model="selectedTag.attributes.textColor">
        </div>
      </div>

      <div class="mt-4 pt-4 border-t-2 flex gap-2 flex">
        <input type="submit" value="Delete"
               v-if="selectedTag.id"
               class="rounded-md bg-red-500 px-4 py-1.5 text-white cursor-pointer hover:bg-red-800"
               @click="deleteTag">
        <div class="flex-grow"></div>
        <input type="submit" value="Edit Patterns"
               v-if="selectedTag.id"
               class="rounded-md bg-blue-500 px-4 py-1.5 text-white cursor-pointer hover:bg-blue-800"
               @click="$router.push('/tags/' + selectedTag.id)">
        <input type="submit" value="Save"
               class="rounded-md bg-blue-500 px-4 py-1.5 text-white cursor-pointer hover:bg-blue-800"
               @click="submitTag">
      </div>
    </modal>
  </div>
</template>

<script>
import Breadcrumbs from "../components/Breadcrumbs.vue";
import Card from "../components/Card.vue";
import Modal from "../components/Modal.vue";

export default {
  name: "Tags",
  components: {Modal, Card, Breadcrumbs},
  data() {
    return {
      busy: false,
      selectedTag: null,
    }
  },
  computed: {
    tags() {
      return this.$store.getters.getTags()
    },
    categories() {
      return this.$store.getters.getCategories()
    }
  },
  mounted() {
    this.$store.dispatch('fetchTags')
    this.$store.dispatch('fetchCategories')
  },
  methods: {
    showModal(tag) {
      if (tag == null) {
        tag = {
          attributes: {
            name: 'New Tag',
            color: '#E5E7EB',
            textColor: '#000000'
          },
          relationships: {
            category: {
              data: null
            }
          }
        }
      } else {
        tag = JSON.parse(JSON.stringify(tag))
      }
      this.selectedTag = tag
    },
    submitTag() {
      if (this.selectedTag == null) return;
      const action = this.selectedTag.id == null ? 'createTag' : 'updateTag'

      const selectedCategoryId = parseInt(this.$refs.categorySelector.value)
      this.selectedTag.relationships.category.data = selectedCategoryId >= 0
          ? { type: 'categories', id: selectedCategoryId }
          : null

      this.$store.dispatch(action, this.selectedTag).then(() => {
        this.selectedTag = null
      })
    },
    deleteTag() {
      this.$store.dispatch('deleteTag', { id: this.selectedTag.id }).then(() => {
        this.selectedTag = null
      })
    },
    hideModal() {
      this.selectedTag = null
    },
    getTagCategoryName(tag) {
      if (!tag.relationships.category.data) return '-'
      const category = this.$store.getters.findCategory(tag.relationships.category.data.id)
      if (!category) return '-'
      return category.attributes.name
    }
  }
}
</script>

<style scoped>

</style>