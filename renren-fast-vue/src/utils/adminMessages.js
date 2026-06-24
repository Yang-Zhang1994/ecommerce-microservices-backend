/**
 * Shared admin UI toast / confirm copy (English).
 */
export const ADMIN_MSG = {
  OPERATION_SUCCESS: 'Operation successful',
  OPERATION_FAILED: 'Operation failed',
  CANCELLED: 'Cancelled',
  SAVE_FAILED: 'Save failed',
  UPLOAD_FAILED: 'Upload failed',
  NETWORK_ERROR: 'Network error, please try again later',
  CONFIRM: 'Confirm',
  CANCEL: 'Cancel',
  TIP: 'Tip',
  QUERY: 'Query',
  ADD: 'Add',
  EDIT: 'Edit',
  DELETE: 'Delete',
  BATCH_DELETE: 'Batch Delete',
  ACTIONS: 'Actions',
  FIELD_REQUIRED: 'This field is required'
}

/** Standard Element-UI delete confirmation. */
export function deleteConfirmMessage(id, ids) {
  const action = id ? 'delete' : 'batch delete'
  return `Are you sure you want to [${action}] [id=${ids.join(',')}]?`
}

export function deleteConfirmOptions(id, ids) {
  return {
    confirmButtonText: ADMIN_MSG.CONFIRM,
    cancelButtonText: ADMIN_MSG.CANCEL,
    type: 'warning'
  }
}
