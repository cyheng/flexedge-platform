<template>
    <div class="input-group">
        <input v-model="newItem" class="form-control" placeholder="add shopping list item" type="text">
        <span class="input-group-btn">
      <button class="btn btn-primary" @click="emitAdd">
        <i class="fa fa-plus-square-o fa-lg"> </i><span>Add</span>
      </button>
    </span>
    </div>
</template>

<script>
export default {
    data() {
        return {
            newItem: ''
        }
    },
    methods: {
        emitAdd() {
            this.$emit('addItem', this.newItem);
        }
    }
}
</script>

<style>
</style>